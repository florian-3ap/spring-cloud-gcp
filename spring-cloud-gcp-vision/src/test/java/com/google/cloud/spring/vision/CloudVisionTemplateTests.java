/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.spring.vision;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageContext;
import com.google.protobuf.ByteString;
import com.google.rpc.Status;
import io.grpc.Status.Code;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * Unit tests for the {@link CloudVisionTemplate}.
 *
 * @since 1.1
 */
public class CloudVisionTemplateTests {

  /** Used to test exception messages and types. * */
  @Rule public ExpectedException expectedException = ExpectedException.none();

  // Resource representing a fake image blob
  private static final Resource FAKE_IMAGE = new ByteArrayResource("fake_image".getBytes());

  private static final BatchAnnotateImagesResponse DEFAULT_API_RESPONSE =
      BatchAnnotateImagesResponse.newBuilder()
          .addResponses(AnnotateImageResponse.getDefaultInstance())
          .build();

  private ImageAnnotatorClient imageAnnotatorClient;

  private CloudVisionTemplate cloudVisionTemplate;

  @Before
  public void setupVisionTemplateMock() {
    this.imageAnnotatorClient = Mockito.mock(ImageAnnotatorClient.class);
    this.cloudVisionTemplate = new CloudVisionTemplate(this.imageAnnotatorClient);
  }

  @Test
  public void testAddImageContext_analyzeImage() throws IOException {
    when(this.imageAnnotatorClient.batchAnnotateImages(any(BatchAnnotateImagesRequest.class)))
        .thenReturn(DEFAULT_API_RESPONSE);

    ImageContext imageContext = Mockito.mock(ImageContext.class);

    this.cloudVisionTemplate.analyzeImage(FAKE_IMAGE, imageContext, Type.FACE_DETECTION);

    BatchAnnotateImagesRequest expectedRequest =
        BatchAnnotateImagesRequest.newBuilder()
            .addRequests(
                AnnotateImageRequest.newBuilder()
                    .addFeatures(Feature.newBuilder().setType(Type.FACE_DETECTION))
                    .setImageContext(imageContext)
                    .setImage(
                        Image.newBuilder()
                            .setContent(ByteString.readFrom(FAKE_IMAGE.getInputStream()))
                            .build()))
            .build();

    verify(this.imageAnnotatorClient, times(1)).batchAnnotateImages(expectedRequest);
  }

  @Test
  public void testAddImageContext_extractText() throws IOException {
    when(this.imageAnnotatorClient.batchAnnotateImages(any(BatchAnnotateImagesRequest.class)))
        .thenReturn(DEFAULT_API_RESPONSE);

    ImageContext imageContext = Mockito.mock(ImageContext.class);

    this.cloudVisionTemplate.extractTextFromImage(FAKE_IMAGE, imageContext);

    BatchAnnotateImagesRequest expectedRequest =
        BatchAnnotateImagesRequest.newBuilder()
            .addRequests(
                AnnotateImageRequest.newBuilder()
                    .addFeatures(Feature.newBuilder().setType(Type.TEXT_DETECTION))
                    .setImageContext(imageContext)
                    .setImage(
                        Image.newBuilder()
                            .setContent(ByteString.readFrom(FAKE_IMAGE.getInputStream()))
                            .build()))
            .build();

    verify(this.imageAnnotatorClient, times(1)).batchAnnotateImages(expectedRequest);
  }

  @Test
  public void testEmptyClientResponseError() {
    when(this.imageAnnotatorClient.batchAnnotateImages(any(BatchAnnotateImagesRequest.class)))
        .thenReturn(BatchAnnotateImagesResponse.getDefaultInstance());

    this.expectedException.expect(CloudVisionException.class);
    this.expectedException.expectMessage(
        "Failed to receive valid response Vision APIs; empty response received.");

    this.cloudVisionTemplate.analyzeImage(FAKE_IMAGE, Type.TEXT_DETECTION);
  }

  @Test
  public void testExtractTextError() {
    AnnotateImageResponse response =
        AnnotateImageResponse.newBuilder()
            .setError(
                Status.newBuilder()
                    .setCode(Code.INTERNAL.value())
                    .setMessage("Error Message from Vision API."))
            .build();

    BatchAnnotateImagesResponse responseBatch =
        BatchAnnotateImagesResponse.newBuilder().addResponses(response).build();

    when(this.imageAnnotatorClient.batchAnnotateImages(any(BatchAnnotateImagesRequest.class)))
        .thenReturn(responseBatch);

    this.expectedException.expect(CloudVisionException.class);
    this.expectedException.expectMessage("Error Message from Vision API.");

    this.cloudVisionTemplate.extractTextFromImage(FAKE_IMAGE);
  }

  @Test
  public void testResourceReadingError() {
    this.expectedException.expect(CloudVisionException.class);
    this.expectedException.expectMessage("Failed to read image bytes from provided resource.");

    this.cloudVisionTemplate.analyzeImage(new BadResource(), Type.LABEL_DETECTION);
  }

  private static final class BadResource extends AbstractResource {
    @Override
    public String getDescription() {
      return "bad resource";
    }

    @Override
    public InputStream getInputStream() throws IOException {
      throw new IOException("Failed to open resource.");
    }
  }
}

/*
 * Copyright 2017-2020 the original author or authors.
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

package com.google.cloud.spring.pubsub.core;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.PublisherFactory;
import com.google.cloud.spring.pubsub.support.SubscriberFactory;
import com.google.cloud.spring.pubsub.support.converter.ConvertedAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.converter.ConvertedBasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.google.pubsub.v1.PubsubMessage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * Default implementation of {@link PubSubOperations}.
 *
 * <p>The main Google Cloud Pub/Sub integration component for publishing to topics and consuming
 * messages from subscriptions asynchronously or by pulling.
 */
public class PubSubTemplate implements PubSubOperations {

  private final PubSubPublisherTemplate pubSubPublisherTemplate;

  private final PubSubSubscriberTemplate pubSubSubscriberTemplate;

  /**
   * Default {@link PubSubTemplate} constructor.
   *
   * @param publisherFactory the {@link com.google.cloud.pubsub.v1.Publisher} factory to publish to
   *     topics.
   * @param subscriberFactory the {@link com.google.cloud.pubsub.v1.Subscriber} factory to subscribe
   *     to subscriptions.
   */
  public PubSubTemplate(PublisherFactory publisherFactory, SubscriberFactory subscriberFactory) {
    Assert.notNull(publisherFactory, "The publisherFactory can't be null.");
    Assert.notNull(subscriberFactory, "The subscriberFactory can't be null.");

    this.pubSubPublisherTemplate = new PubSubPublisherTemplate(publisherFactory);
    this.pubSubSubscriberTemplate = new PubSubSubscriberTemplate(subscriberFactory);
  }

  /**
   * Default {@link PubSubTemplate} constructor that uses a {@link PubSubPublisherTemplate} and a
   * {@link PubSubSubscriberTemplate}.
   *
   * @param pubSubPublisherTemplate the {@link PubSubPublisherTemplate} to publish to topics.
   * @param pubSubSubscriberTemplate the {@link PubSubSubscriberTemplate} to subscribe to
   *     subscriptions or pull messages
   * @since 1.1
   */
  public PubSubTemplate(
      PubSubPublisherTemplate pubSubPublisherTemplate,
      PubSubSubscriberTemplate pubSubSubscriberTemplate) {
    Assert.notNull(pubSubPublisherTemplate, "The pubSubPublisherTemplate can't be null.");
    Assert.notNull(pubSubSubscriberTemplate, "The pubSubSubscriberTemplate can't be null.");

    this.pubSubPublisherTemplate = pubSubPublisherTemplate;
    this.pubSubSubscriberTemplate = pubSubSubscriberTemplate;
  }

  public PubSubPublisherTemplate getPubSubPublisherTemplate() {
    return this.pubSubPublisherTemplate;
  }

  public PubSubSubscriberTemplate getPubSubSubscriberTemplate() {
    return this.pubSubSubscriberTemplate;
  }

  public PubSubMessageConverter getMessageConverter() {
    return this.pubSubPublisherTemplate.getMessageConverter();
  }

  public void setMessageConverter(PubSubMessageConverter messageConverter) {
    Assert.notNull(messageConverter, "A valid Pub/Sub message converter is required.");

    this.pubSubPublisherTemplate.setMessageConverter(messageConverter);
    this.pubSubSubscriberTemplate.setMessageConverter(messageConverter);
  }

  /**
   * Uses the configured message converter to first convert the payload and headers to a {@link
   * PubsubMessage} and then publish it.
   */
  @Override
  public <T> ListenableFuture<String> publish(
      String topic, T payload, Map<String, String> headers) {
    return this.pubSubPublisherTemplate.publish(topic, payload, headers);
  }

  @Override
  public <T> ListenableFuture<String> publish(String topic, T payload) {
    return this.pubSubPublisherTemplate.publish(topic, payload, null);
  }

  @Override
  public ListenableFuture<String> publish(final String topic, PubsubMessage pubsubMessage) {
    return this.pubSubPublisherTemplate.publish(topic, pubsubMessage);
  }

  @Override
  public Subscriber subscribe(
      String subscription, Consumer<BasicAcknowledgeablePubsubMessage> messageConsumer) {
    return this.pubSubSubscriberTemplate.subscribe(subscription, messageConsumer);
  }

  @Override
  public <T> Subscriber subscribeAndConvert(
      String subscription,
      Consumer<ConvertedBasicAcknowledgeablePubsubMessage<T>> messageConsumer,
      Class<T> payloadType) {
    return this.pubSubSubscriberTemplate.subscribeAndConvert(
        subscription, messageConsumer, payloadType);
  }

  @Override
  public List<AcknowledgeablePubsubMessage> pull(
      String subscription, Integer maxMessages, Boolean returnImmediately) {
    return this.pubSubSubscriberTemplate.pull(subscription, maxMessages, returnImmediately);
  }

  @Override
  public ListenableFuture<List<AcknowledgeablePubsubMessage>> pullAsync(
      String subscription, Integer maxMessages, Boolean returnImmediately) {
    return this.pubSubSubscriberTemplate.pullAsync(subscription, maxMessages, returnImmediately);
  }

  @Override
  public <T> List<ConvertedAcknowledgeablePubsubMessage<T>> pullAndConvert(
      String subscription, Integer maxMessages, Boolean returnImmediately, Class<T> payloadType) {
    return this.pubSubSubscriberTemplate.pullAndConvert(
        subscription, maxMessages, returnImmediately, payloadType);
  }

  @Override
  public <T> ListenableFuture<List<ConvertedAcknowledgeablePubsubMessage<T>>> pullAndConvertAsync(
      String subscription, Integer maxMessages, Boolean returnImmediately, Class<T> payloadType) {
    return this.pubSubSubscriberTemplate.pullAndConvertAsync(
        subscription, maxMessages, returnImmediately, payloadType);
  }

  @Override
  public List<PubsubMessage> pullAndAck(
      String subscription, Integer maxMessages, Boolean returnImmediately) {
    return this.pubSubSubscriberTemplate.pullAndAck(subscription, maxMessages, returnImmediately);
  }

  @Override
  public ListenableFuture<List<PubsubMessage>> pullAndAckAsync(
      String subscription, Integer maxMessages, Boolean returnImmediately) {
    return this.pubSubSubscriberTemplate.pullAndAckAsync(
        subscription, maxMessages, returnImmediately);
  }

  @Override
  public PubsubMessage pullNext(String subscription) {
    return this.pubSubSubscriberTemplate.pullNext(subscription);
  }

  @Override
  public ListenableFuture<PubsubMessage> pullNextAsync(String subscription) {
    return this.pubSubSubscriberTemplate.pullNextAsync(subscription);
  }

  @Override
  public ListenableFuture<Void> ack(
      Collection<? extends AcknowledgeablePubsubMessage> acknowledgeablePubsubMessages) {
    return this.pubSubSubscriberTemplate.ack(acknowledgeablePubsubMessages);
  }

  @Override
  public ListenableFuture<Void> nack(
      Collection<? extends AcknowledgeablePubsubMessage> acknowledgeablePubsubMessages) {
    return this.pubSubSubscriberTemplate.nack(acknowledgeablePubsubMessages);
  }

  @Override
  public ListenableFuture<Void> modifyAckDeadline(
      Collection<? extends AcknowledgeablePubsubMessage> acknowledgeablePubsubMessages,
      int ackDeadlineSeconds) {
    return this.pubSubSubscriberTemplate.modifyAckDeadline(
        acknowledgeablePubsubMessages, ackDeadlineSeconds);
  }

  public PublisherFactory getPublisherFactory() {
    return this.pubSubPublisherTemplate.getPublisherFactory();
  }

  public SubscriberFactory getSubscriberFactory() {
    return this.pubSubSubscriberTemplate.getSubscriberFactory();
  }
}

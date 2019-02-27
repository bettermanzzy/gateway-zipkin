/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.spring.boot2.gateway.tracing;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.INCLUDE;
import static javax.servlet.DispatcherType.REQUEST;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import brave.http.HttpClientAdapter;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.servlet.TracingFilter;

@Configuration
//@ConditionalOnProperty(value = CONFIG_TRACING_ENABLED_KEY, havingValue = "true", matchIfMissing = true)
public class SpringTracingConfigurationGateway {

  //@SuppressWarnings({"rawtypes","unchecked"})
  @Bean
  FilterRegistrationBean<javax.servlet.Filter> traceWebFilter(HttpTracing httpTracing) {
    FilterRegistrationBean<javax.servlet.Filter> filterRegistrationBean = new FilterRegistrationBean<>(TracingFilter.create(httpTracing));
    filterRegistrationBean.setDispatcherTypes(ASYNC, ERROR, FORWARD, INCLUDE, REQUEST);
    filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return filterRegistrationBean;
  }

  @SuppressWarnings("unchecked")
  @Bean
  HttpClientHandler<ServerHttpRequest, ServerHttpResponse> httpClientHandler(HttpTracing httpTracing) {
    return HttpClientHandler.create(httpTracing, new GatewayHttpClientAdapter());
  }

  @Bean
  TracePreGatewayFilter tracePreZuulFilter(
      HttpTracing tracing,
      HttpClientHandler<ServerHttpRequest, ServerHttpResponse> handler) {
      System.out.println("PreGatewayOK");
    return new TracePreGatewayFilter(tracing, handler);
  }

//  @Bean
//  TracePostGatewayFilter tracePostZuulFilter(
//      HttpTracing tracing,
//      HttpClientHandler<ServerHttpRequest, ServerHttpResponse> handler) {
//
//    return new TracePostGatewayFilter(tracing, handler);
//  }

  private static class GatewayHttpClientAdapter extends HttpClientAdapter<ServerHttpRequest, ServerHttpResponse> {

    @Nullable
    @Override
    public String method(@Nonnull ServerHttpRequest request) {
      return Objects.requireNonNull(request.getMethod()).name();
    }

    @Nullable
    @Override
    public String url(@Nonnull ServerHttpRequest request) {
      return request.getURI().getHost();
    }

    @Nullable
    @Override
    public String requestHeader(@Nonnull ServerHttpRequest request, @Nonnull String name) {
      return request.getHeaders().getFirst(name);
    }

    @Nullable
    @Override
    public Integer statusCode(@Nonnull ServerHttpResponse serverHttpResponse) {
      return Objects.requireNonNull(serverHttpResponse.getStatusCode()).value() == 0 ? 500 : serverHttpResponse.getStatusCode().value();
    }
  }
}



package org.apache.servicecomb.springboot2.starter.gateway;

import static org.apache.servicecomb.core.Const.CSE_CONTEXT;
import static org.apache.servicecomb.core.Const.SRC_MICROSERVICE;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceDefinition;
import org.aspectj.weaver.ast.Or;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;

import reactor.core.publisher.Mono;

@Component
public class ContextHeaderGatewayFilter implements GlobalFilter, Ordered {

  private static String microserviceName ;

  static {
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
    microserviceName = microserviceDefinition.getMicroserviceName();
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
    ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
    ServerHttpRequest request = builder.header(SRC_MICROSERVICE,microserviceName).build();
    //request = saveHeadersAsInvocationContext(request,exchange);
    //chain.filter(exchange.mutate().request(builder.build()).build());

    return chain.filter(exchange.mutate().request(request).build()).then();
  }

//  private ServerHttpRequest saveHeadersAsInvocationContext(ServerHttpRequest request,ServerWebExchange exchange) {
//    try {
//      //ctx.addZuulRequestHeader(CSE_CONTEXT, JsonUtils.writeValueAsString(ctx.getZuulRequestHeaders()));
//      return  request.mutate().header(CSE_CONTEXT, JsonUtils.writeValueAsString(exchange.getRequest().getHeaders().toSingleValueMap())).build();
//
//    } catch (JsonProcessingException e) {
//
//      throw new IllegalStateException("Unable to write request headers as json to " + CSE_CONTEXT, e);
//    }
//  }


  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}





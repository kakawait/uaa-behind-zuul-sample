package com.kakawait;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.constants.ZuulHeaders;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.ProxyRouteLocator;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Thibaud Leprêtre
 */
class ForwardedHeaderFilter extends ZuulFilter {
    private static final String PREFIX_FORWARDED_HEADER = "X-Forwarded-Prefix";

    private static final String HOST_FORWARDED_HEADER = "X-Forwarded-Host";

    private final ProxyRouteLocator routeLocator;

    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    ForwardedHeaderFilter(ProxyRouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 9;
    }

    @Override
    public boolean shouldFilter() {
        return !RequestContext.getCurrentContext().containsKey("forward.to");
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String requestURI = this.urlPathHelper.getPathWithinApplication(request);
        ProxyRouteLocator.ProxyRouteSpec route = this.routeLocator.getMatchingRoute(requestURI);
        // Use ServletUriComponentsBuilder that aware of X-Forwarded-* headers
        // So if request comes from another proxy X-Forwarded-* headers will be take care
        UriComponents uriComponents = ServletUriComponentsBuilder.fromRequest(request).build();
        // todo: check if port == -1 that will not break
        ctx.addZuulRequestHeader(HOST_FORWARDED_HEADER, uriComponents.getHost() + ":"
                                                        + String.valueOf(uriComponents.getPort()));
        ctx.addZuulRequestHeader(ZuulHeaders.X_FORWARDED_PROTO, uriComponents.getScheme());
        ctx.addZuulRequestHeader("X-Forwarded-Port",
                                 String.valueOf(ctx.getRequest().getServerPort()));
        String prefix = request.getHeader(PREFIX_FORWARDED_HEADER) != null ? request.getHeader(PREFIX_FORWARDED_HEADER)
                                                                           : "";
        if (StringUtils.hasText(route.getPrefix())) {
            try {
                prefix = new URI(prefix + "/" + route.getPrefix()).normalize().toString();
            } catch (URISyntaxException e) {
                prefix = route.getPrefix();
            }
            ctx.addZuulRequestHeader(PREFIX_FORWARDED_HEADER, prefix);
        }
        return null;
    }
}

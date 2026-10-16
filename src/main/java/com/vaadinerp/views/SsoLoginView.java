package com.vaadinerp.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadinerp.security.sso.SsoService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Titik awal "Login with SSO": membangun URL authorize (PKCE), menyimpan state/verifier/redirect
 * sekali-pakai di VaadinSession, lalu mengalihkan browser ke Authentik. Tidak merender UI apa pun.
 * Rute ini dikecualikan dari penjaga sesi (lihat SecurityNavigationGuard).
 */
@Route("sso/login")
public class SsoLoginView extends Div implements BeforeEnterObserver {

    public static final String ATTR_STATE = "SSO_STATE";
    public static final String ATTR_VERIFIER = "SSO_VERIFIER";
    public static final String ATTR_REDIRECT = "SSO_REDIRECT";

    private final SsoService sso;

    public SsoLoginView(SsoService sso) {
        this.sso = sso;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sso.isEnabled()) {
            event.forwardTo("login");
            return;
        }
        String redirectUri = buildRedirectUri();
        String state = sso.newState();
        String verifier = sso.newCodeVerifier();

        VaadinSession s = VaadinSession.getCurrent();
        s.setAttribute(ATTR_STATE, state);
        s.setAttribute(ATTR_VERIFIER, verifier);
        s.setAttribute(ATTR_REDIRECT, redirectUri);

        event.forwardToUrl(sso.buildAuthorizeUrl(redirectUri, state, verifier));
    }

    /**
     * Susun redirect URI dari request saat ini: {@code <scheme>://<host>[:<port>]/sso/callback}.
     * Di belakang reverse proxy, set {@code server.forward-headers-strategy} agar scheme/host benar.
     */
    static String buildRedirectUri() {
        HttpServletRequest req = ((VaadinServletRequest) VaadinServletRequest.getCurrent()).getHttpServletRequest();
        String scheme = req.getScheme();
        int port = req.getServerPort();
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        StringBuilder sb = new StringBuilder(scheme).append("://").append(req.getServerName());
        if (!defaultPort) {
            sb.append(":").append(port);
        }
        return sb.append("/sso/callback").toString();
    }
}

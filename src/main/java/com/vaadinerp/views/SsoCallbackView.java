package com.vaadinerp.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadinerp.security.service.SessionSecurityService;
import com.vaadinerp.security.service.SessionSecurityService.AlreadyLoggedInException;
import com.vaadinerp.security.service.SessionSecurityService.MaintenanceException;
import com.vaadinerp.security.sso.SsoService;

/**
 * Endpoint callback OIDC: menerima {@code code}+{@code state} dari Authentik, memverifikasi state,
 * menukar code menjadi identitas, lalu login lewat {@link SessionSecurityService#loginViaSso(String, String)}.
 * Berhasil -> reload ke portal; gagal -> kembali ke /login dengan pesan (disimpan di session).
 * Rute ini dikecualikan dari penjaga sesi (lihat SecurityNavigationGuard).
 */
@Route("sso/callback")
public class SsoCallbackView extends Div implements BeforeEnterObserver {

    /** Pesan error SSO untuk ditampilkan LoginView setelah redirect kembali. */
    public static final String ATTR_ERROR = "SSO_ERROR";
    /** Penanda bahwa sesi ini login lewat SSO — dipakai saat logout untuk end_session Authentik. */
    public static final String ATTR_SSO_LOGIN = "SSO_LOGIN";

    private final SsoService sso;
    private final SessionSecurityService security;

    public SsoCallbackView(SsoService sso, SessionSecurityService security) {
        this.sso = sso;
        this.security = security;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sso.isEnabled()) {
            event.forwardTo("login");
            return;
        }
        // Idempoten: setelah login sukses, sesi diganti (anti session-fixation) sehingga Vaadin memuat ulang
        // halaman pada URL callback yang SAMA. State/PKCE sudah terpakai, jadi pembacaan kedua akan gagal
        // dengan "Invalid SSO session" padahal user sudah masuk. Bila sudah login, langsung ke portal.
        if (security.isLoggedIn()) {
            event.getUI().getPage().setLocation("/");
            return;
        }
        VaadinSession s = VaadinSession.getCurrent();
        QueryParameters qp = event.getLocation().getQueryParameters();
        String code = qp.getSingleParameter("code").orElse(null);
        String state = qp.getSingleParameter("state").orElse(null);
        String oauthError = qp.getSingleParameter("error").orElse(null);

        String expectedState = (String) s.getAttribute(SsoLoginView.ATTR_STATE);
        String verifier = (String) s.getAttribute(SsoLoginView.ATTR_VERIFIER);
        String redirectUri = (String) s.getAttribute(SsoLoginView.ATTR_REDIRECT);
        // state/verifier sekali pakai
        s.setAttribute(SsoLoginView.ATTR_STATE, null);
        s.setAttribute(SsoLoginView.ATTR_VERIFIER, null);
        s.setAttribute(SsoLoginView.ATTR_REDIRECT, null);

        if (oauthError != null) {
            fail(event, s, "SSO sign-in was cancelled.");
            return;
        }
        if (code == null || state == null || expectedState == null || !expectedState.equals(state)
                || verifier == null || redirectUri == null) {
            fail(event, s, "Invalid SSO session. Please try again.");
            return;
        }

        SsoService.SsoIdentity identity = sso.exchange(code, redirectUri, verifier);
        if (identity == null) {
            fail(event, s, "Could not verify your SSO account.");
            return;
        }
        String username = identity.username();

        try {
            boolean ok = security.loginViaSso(username, identity.sub());
            if (!ok) {
                fail(event, s, "Account '" + username + "' is not registered in this application.");
                return;
            }
            // Tandai sesi (baru, pasca reinit) sebagai login SSO untuk keperluan logout end_session.
            VaadinSession cur = VaadinSession.getCurrent();
            if (cur != null) {
                cur.setAttribute(ATTR_SSO_LOGIN, Boolean.TRUE);
            }
            // Sukses: reload bersih ke portal (session sudah di-reinit di establishSession).
            event.getUI().getPage().setLocation("/");
        } catch (AlreadyLoggedInException | MaintenanceException ex) {
            fail(event, s, ex.getMessage());
        }
    }

    private void fail(BeforeEnterEvent event, VaadinSession s, String msg) {
        s.setAttribute(ATTR_ERROR, msg);
        event.forwardTo("login");
    }
}

package com.vaadinerp.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadinerp.security.service.SessionSecurityService;

@Route("login")
@PageTitle("Login | ERP Enterprise")
public class LoginView extends Div {

        private final SessionSecurityService securityService;

        public LoginView(SessionSecurityService securityService) {
                this.securityService = securityService;

                setSizeFull();
                getStyle()
                                .set("display", "flex")
                                .set("align-items", "center")
                                .set("justify-content", "center")
                                .set("min-height", "100vh")
                                .set("margin", "0")
                                .set("padding", "24px")
                                .set("box-sizing", "border-box")
                                .set("font-family",
                                                "'Segoe UI', 'Inter', -apple-system, BlinkMacSystemFont, sans-serif")
                                .set("background", "#0a1628")
                                .set("position", "relative")
                                .set("overflow", "hidden");

                // ── Animated background glow orbs ──
                Div glowOrb1 = createGlowOrb("500px", "#0050b3", "-10%", "-5%", "0.15");
                Div glowOrb2 = createGlowOrb("400px", "#0070F2", "60%", "70%", "0.10");
                Div glowOrb3 = createGlowOrb("300px", "#40A0FF", "80%", "10%", "0.08");
                add(glowOrb1, glowOrb2, glowOrb3);

                // ── Subtle dot grid pattern ──
                Div pattern = new Div();
                pattern.getStyle()
                                .set("position", "absolute")
                                .set("inset", "0")
                                .set("background-image", "radial-gradient(rgba(255,255,255,0.04) 1px, transparent 1px)")
                                .set("background-size", "28px 28px")
                                .set("pointer-events", "none")
                                .set("z-index", "1");
                add(pattern);

                // ═══════════════════════════════════════
                // MAIN CARD — centered login form
                // ═══════════════════════════════════════
                VerticalLayout card = new VerticalLayout();
                card.setWidth("420px");
                card.setMaxWidth("100%");
                card.setPadding(false);
                card.setSpacing(false);
                card.setAlignItems(FlexComponent.Alignment.CENTER);
                card.getStyle()
                                .set("background", "rgba(255, 255, 255, 0.97)")
                                .set("border-radius", "16px")
                                .set("box-shadow", "0 24px 80px rgba(0,0,0,0.45), 0 0 0 1px rgba(255,255,255,0.06)")
                                .set("padding", "44px 36px 36px 36px")
                                .set("position", "relative")
                                .set("z-index", "2")
                                .set("overflow", "hidden");

                // Blue accent bar at the top of card
                Div topAccent = new Div();
                topAccent.getStyle()
                                .set("position", "absolute")
                                .set("top", "0").set("left", "0").set("right", "0")
                                .set("height", "4px")
                                .set("background", "linear-gradient(90deg, #0050b3, #0070F2, #40A0FF)")
                                .set("border-radius", "16px 16px 0 0");
                card.add(topAccent);

                // ── Logo ──
                Image logoImage = new Image("images/logo_growth.png", "PT. GROWTH ASIA");
                logoImage.getStyle()
                                .set("width", "72px").set("height", "72px")
                                .set("border-radius", "16px")
                                .set("object-fit", "contain")
                                .set("box-shadow", "0 8px 24px rgba(0, 112, 242, 0.25)")
                                .set("margin-bottom", "20px");
                card.add(logoImage);

                // ── Title ──
                H2 title = new H2("PT. GROWTH ASIA");
                title.getStyle()
                                .set("margin", "0")
                                .set("font-size", "1.65rem")
                                .set("font-weight", "700")
                                .set("color", "#111827")
                                .set("letter-spacing", "-0.3px");
                card.add(title);

                Span edition = new Span("ENTERPRISE EDITION");
                edition.getStyle()
                                .set("font-size", "0.68rem")
                                .set("letter-spacing", "3px")
                                .set("color", "#0070F2")
                                .set("font-weight", "600")
                                .set("margin-bottom", "28px");
                card.add(edition);

                // ── Username field ──
                TextField usernameField = new TextField("User");
                usernameField.setWidthFull();
                usernameField.setPlaceholder("Masukkan username");
                usernameField.setPrefixComponent(VaadinIcon.USER.create());
                usernameField.getStyle().set("--lumo-border-radius-m", "8px");
                card.add(usernameField);

                // spacer between fields
                Div fieldSpacer = new Div();
                fieldSpacer.getStyle().set("height", "12px");
                card.add(fieldSpacer);

                // ── Password field ──
                PasswordField passwordField = new PasswordField("Password");
                passwordField.setWidthFull();
                passwordField.setPlaceholder("Masukkan password");
                passwordField.setPrefixComponent(VaadinIcon.LOCK.create());
                passwordField.getStyle().set("--lumo-border-radius-m", "8px");
                card.add(passwordField);

                // ── Log On button ──
                Button loginBtn = new Button("Log On");
                loginBtn.setWidthFull();
                loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                loginBtn.getStyle()
                                .set("background", "#0070F2")
                                .set("border-radius", "8px")
                                .set("height", "44px")
                                .set("font-weight", "600")
                                .set("font-size", "0.95rem")
                                .set("margin-top", "20px")
                                .set("cursor", "pointer")
                                .set("transition", "background 0.2s ease, box-shadow 0.2s ease")
                                .set("box-shadow", "0 4px 14px rgba(0, 112, 242, 0.30)");
                card.add(loginBtn);

                loginBtn.addClickListener(e -> {
                        String u = usernameField.getValue();
                        String p = passwordField.getValue();
                        if (this.securityService.login(u, p)) {
                                Notification.show("Selamat datang, " + u + "!", 3000, Notification.Position.TOP_CENTER);
                                UI.getCurrent().navigate("");
                        } else {
                                Notification n = Notification.show("Username atau password salah!", 4000,
                                                Notification.Position.TOP_CENTER);
                                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                });

                // Enter key shortcut
                loginBtn.addClickShortcut(com.vaadin.flow.component.Key.ENTER);

                // ── Divider ──
                Div divider = new Div();
                divider.setWidthFull();
                divider.getStyle()
                                .set("border-top", "1px solid #e5e7eb")
                                .set("margin", "24px 0 18px 0");
                card.add(divider);

                // ── Demo credentials info box ──
                Div hintBox = new Div();
                hintBox.setWidthFull();
                hintBox.getStyle()
                                .set("background", "#F0F6FF")
                                .set("border-left", "3px solid #0070F2")
                                .set("border-radius", "0 8px 8px 0")
                                .set("padding", "14px 16px")
                                .set("font-size", "0.8rem")
                                .set("color", "#374151")
                                .set("line-height", "1.6");

                hintBox.getElement().setProperty("innerHTML",
                                "<div style='font-weight:600;color:#0070F2;margin-bottom:6px;font-size:0.72rem;letter-spacing:0.5px;text-transform:uppercase'>Akun Demo</div>"
                                                +
                                                "<div style='display:flex;gap:28px'>" +
                                                "<div><span style='font-weight:600'>admin</span> / admin<br><span style='color:#6b7280;font-size:0.72rem'>Full Access</span></div>"
                                                +
                                                "<div><span style='font-weight:600'>staff</span> / staff<br><span style='color:#6b7280;font-size:0.72rem'>Read Only</span></div>"
                                                +
                                                "</div>");
                card.add(hintBox);

                add(card);

                // ── Footer below card ──
                Paragraph footer = new Paragraph("© 2026 ERP Enterprise • Powered by Tommy");
                footer.getStyle()
                                .set("position", "absolute")
                                .set("bottom", "18px")
                                .set("font-size", "0.7rem")
                                .set("color", "rgba(255,255,255,0.25)")
                                .set("z-index", "2")
                                .set("text-align", "center");
                add(footer);
        }

        /**
         * Creates a soft radial glow orb for the background ambience.
         */
        private Div createGlowOrb(String size, String color, String top, String left, String opacity) {
                Div orb = new Div();
                orb.getStyle()
                                .set("position", "absolute")
                                .set("width", size).set("height", size)
                                .set("background", "radial-gradient(circle, " + color + " 0%, transparent 70%)")
                                .set("border-radius", "50%")
                                .set("top", top).set("left", left)
                                .set("opacity", opacity)
                                .set("filter", "blur(60px)")
                                .set("pointer-events", "none")
                                .set("z-index", "0");
                return orb;
        }
}

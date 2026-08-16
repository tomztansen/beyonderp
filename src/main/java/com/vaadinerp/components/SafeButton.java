package com.vaadinerp.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;

/**
 * A custom Button that automatically prevents double-clicks by using setDisableOnClick(true).
 * It automatically re-enables itself after the click listener has finished executing.
 */
public class SafeButton extends Button {

    public SafeButton() {
        super();
        setDisableOnClick(true);
    }

    public SafeButton(String text) {
        super(text);
        setDisableOnClick(true);
    }

    public SafeButton(Icon icon) {
        super(icon);
        setDisableOnClick(true);
    }

    public SafeButton(String text, Icon icon) {
        super(text, icon);
        setDisableOnClick(true);
    }

    public SafeButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text);
        setDisableOnClick(true);
        addClickListener(clickListener);
    }

    public SafeButton(Icon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(icon);
        setDisableOnClick(true);
        addClickListener(clickListener);
    }

    public SafeButton(String text, Icon icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
        super(text, icon);
        setDisableOnClick(true);
        addClickListener(clickListener);
    }

    @Override
    public Registration addClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
        return super.addClickListener(event -> {
            try {
                listener.onComponentEvent(event);
            } finally {
                // Ensure the button is re-enabled on the server side after processing
                // so the client receives the enabled state in the response.
                this.setEnabled(true);
            }
        });
    }
}

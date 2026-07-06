import { unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin/register-styles';

import vaadinGridCss from 'themes/vaadinerp/components/vaadin-grid.css?inline';
import vaadinCheckboxCss from 'themes/vaadinerp/components/vaadin-checkbox.css?inline';


if (!document['_vaadintheme_vaadinerp_componentCss']) {
  registerStyles(
        'vaadin-grid',
        unsafeCSS(vaadinGridCss.toString())
      );
      registerStyles(
        'vaadin-checkbox',
        unsafeCSS(vaadinCheckboxCss.toString())
      );
      
  document['_vaadintheme_vaadinerp_componentCss'] = true;
}

if (import.meta.hot) {
  import.meta.hot.accept((module) => {
    window.location.reload();
  });
}


import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/icons/vaadin-iconset.js';
import '@vaadin/icon/theme/lumo/vaadin-icon.js';
import '@vaadin/password-field/theme/lumo/vaadin-password-field.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/app-layout/theme/lumo/vaadin-app-layout.js';
import '@vaadin/dialog/theme/lumo/vaadin-dialog.js';
import '@vaadin/confirm-dialog/theme/lumo/vaadin-confirm-dialog.js';
import '@vaadin/grid/theme/lumo/vaadin-grid.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-sorter.js';
import '@vaadin/checkbox/theme/lumo/vaadin-checkbox.js';
import 'Frontend/generated/jar-resources/gridConnector.ts';
import 'Frontend/generated/jar-resources/vaadin-grid-flow-selection-column.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-column-group.js';
import 'Frontend/generated/jar-resources/lit-renderer.ts';
import '@vaadin/context-menu/theme/lumo/vaadin-context-menu.js';
import 'Frontend/generated/jar-resources/contextMenuConnector.js';
import 'Frontend/generated/jar-resources/contextMenuTargetConnector.js';
import '@vaadin/horizontal-layout/theme/lumo/vaadin-horizontal-layout.js';
import '@vaadin/tabsheet/theme/lumo/vaadin-tabsheet.js';
import '@vaadin/tabs/theme/lumo/vaadin-tabs.js';
import '@vaadin/tabs/theme/lumo/vaadin-tab.js';
import '@vaadin/combo-box/theme/lumo/vaadin-combo-box.js';
import 'Frontend/generated/jar-resources/comboBoxConnector.js';
import '@vaadin/multi-select-combo-box/theme/lumo/vaadin-multi-select-combo-box.js';
import '@vaadin/integer-field/theme/lumo/vaadin-integer-field.js';
import 'Frontend/generated/jar-resources/menubarConnector.js';
import '@vaadin/menu-bar/theme/lumo/vaadin-menu-bar.js';
import '@vaadin/details/theme/lumo/vaadin-details.js';
import '@vaadin/custom-field/theme/lumo/vaadin-custom-field.js';
import 'Frontend/generated/jar-resources/vaadin-big-decimal-field.js';
import '@vaadin/date-picker/theme/lumo/vaadin-date-picker.js';
import 'Frontend/generated/jar-resources/datepickerConnector.js';
import '@vaadin/text-area/theme/lumo/vaadin-text-area.js';
import '@vaadin/select/theme/lumo/vaadin-select.js';
import 'Frontend/generated/jar-resources/selectConnector.js';
import '@vaadin/list-box/theme/lumo/vaadin-list-box.js';
import '@vaadin/item/theme/lumo/vaadin-item.js';
import '@vaadin/radio-group/theme/lumo/vaadin-radio-group.js';
import '@vaadin/radio-group/theme/lumo/vaadin-radio-button.js';
import '@vaadin/date-time-picker/theme/lumo/vaadin-date-time-picker.js';
import '@vaadin/time-picker/theme/lumo/vaadin-time-picker.js';
import 'Frontend/generated/jar-resources/vaadin-time-picker/timepickerConnector.js';
import '@vaadin/upload/theme/lumo/vaadin-upload.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-layout.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-item.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-row.js';
import '@vaadin/app-layout/theme/lumo/vaadin-drawer-toggle.js';
import 'Frontend/generated/jar-resources/dndConnector.js';
import '@vaadin/checkbox-group/theme/lumo/vaadin-checkbox-group.js';
import '@vaadin/split-layout/theme/lumo/vaadin-split-layout.js';
import '@vaadin/number-field/theme/lumo/vaadin-number-field.js';
import '@vaadin/grid/theme/lumo/vaadin-grid-tree-toggle.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '6a0ee45d711cd40e0bcb5d8701c984002a657e75e6e7beb3ca51d3a99a792c3f') {
    pending.push(import('./chunks/chunk-1e4ce7d08db3f2a042c0dc9b58756549ac638729225578c0276519b78101a0df.js'));
  }
  if (key === 'e21adeb826a967cc8349a84e2f7cac2a67d7ce80df1793dc6059db5b6a3ada27') {
    pending.push(import('./chunks/chunk-15af0249255aff77b08a437f8b45bdfe2d0f6ae1b589ff6e1bc8afec857a28ca.js'));
  }
  if (key === '0a355c93c416a2f07c409dd4964914d70c2f10c28276231dcf41381142786b46') {
    pending.push(import('./chunks/chunk-c5a29cbc7539071cce655188c058af86ed39c5cc13e4405c68a6bcc4ffc9d1e7.js'));
  }
  if (key === 'c8c5f9892a4459948dc68d9b4e0e107f25a5eb1c4e76f0c6240ed3d5d83a4b53') {
    pending.push(import('./chunks/chunk-aa9dbf8a2a0cfc114669594cf114ecbddefa22c0dee0d1a8baa9fed8c39cfc14.js'));
  }
  if (key === '4cc13d1a104b45be03cf839600b279079713b6c1a9b5de801c1a19673c0667f1') {
    pending.push(import('./chunks/chunk-74fbd002b526b8edd2bbc6d4ead2bcecf309525d2ab71ab35164610efb25526e.js'));
  }
  if (key === '59c6206cfe6a7ce26e9c71cd6266ca407da4a74d07d925a7fd2eb7b7b67f7a99') {
    pending.push(import('./chunks/chunk-c5a29cbc7539071cce655188c058af86ed39c5cc13e4405c68a6bcc4ffc9d1e7.js'));
  }
  if (key === '314b788cf27457a2cf9a57aec376a2427ac62b833a8aea586f66f41a89a30982') {
    pending.push(import('./chunks/chunk-d3ab655b749a955ddf9aff3c4b4d367ea25636a971992d78e0c859eb4dfb55b0.js'));
  }
  if (key === 'a978fd920b7ccb891c08ad15f1817bc8f0285c5a3448c138f50211ec33d923b7') {
    pending.push(import('./chunks/chunk-affe28f46f8b93d1d3914b0b83b50a69d838f0ef8815991a74112fb4b2dfa233.js'));
  }
  if (key === '4c4c0d2fadccadc37a23394d905748a2e03566a6ccf12ebb2395faf2b8e24b31') {
    pending.push(import('./chunks/chunk-28c06010f7bc396667787eaf16c365f1a43c388d80688a7ca696a60bf4371d39.js'));
  }
  if (key === '6f990da26c1c9d5d0bd281accec8c1c5e1832ab3a33022dda376013467d03cf7') {
    pending.push(import('./chunks/chunk-d3ab655b749a955ddf9aff3c4b4d367ea25636a971992d78e0c859eb4dfb55b0.js'));
  }
  if (key === 'e112fd43802440e524a0e8e4de20a88a4a6200d0bd4ab9278b9bcb2ac166054e') {
    pending.push(import('./chunks/chunk-c5a29cbc7539071cce655188c058af86ed39c5cc13e4405c68a6bcc4ffc9d1e7.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}
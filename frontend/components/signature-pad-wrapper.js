import { LitElement, html } from 'lit';
import SignaturePad from 'signature_pad';

// Ukuran maksimum PNG hasil ekspor (px). Menjaga ukuran data URL tetap kecil walau layar HiDPI.
const MAX_EXPORT_WIDTH = 600;
const MAX_EXPORT_HEIGHT = 200;
// Gambar yang diunggah diperkecil ke kotak ini (rasio dipertahankan) dan ditolak bila filenya lebih besar.
const MAX_UPLOAD_HEIGHT = 200;
const MAX_UPLOAD_BYTES = 1024 * 1024;
// Ekspor dipotong ke area tinta ditambah margin ini (px layar), supaya tanda tangan memenuhi kotaknya di
// dokumen/report dan bukan kecil di tengah kanvas kosong. Piksel dengan alpha di bawah ambang dianggap kosong.
const CROP_PADDING = 8;
const INK_ALPHA_MIN = 10;

// Resolusi kanvas minimal 2x ukuran tampilan. Di layar biasa (devicePixelRatio 1) tanpa ini PNG yang tersimpan
// hanya setengah resolusinya dan pecah saat dicetak; dengan 2x hasil crop tetap tajam di semua perangkat.
const MIN_PIXEL_RATIO = 2;
const pixelRatio = () => Math.max(window.devicePixelRatio || 1, MIN_PIXEL_RATIO);

/**
 * Jadikan latar terang transparan supaya foto tanda tangan di kertas putih tidak tampil sebagai kotak putih
 * di dokumen berwarna. Warna tinta dipertahankan; alpha asli (PNG transparan) tidak pernah dinaikkan.
 */
function whiteToTransparent(ctx, w, h) {
  const img = ctx.getImageData(0, 0, w, h);
  const d = img.data;
  for (let i = 0; i < d.length; i += 4) {
    const lum = 0.299 * d[i] + 0.587 * d[i + 1] + 0.114 * d[i + 2];
    const a = lum >= 235 ? 0 : lum <= 150 ? 255 : ((235 - lum) / 85) * 255;
    d[i + 3] = Math.min(d[i + 3], a);
  }
  ctx.putImageData(img, 0, 0);
}

/**
 * Kanvas tanda tangan (library signature_pad). Dipakai oleh SignaturePadField.java.
 *
 * Siklus hidup: semua pendengar (pointer lewat pad.off(), endStroke, ResizeObserver) dilepas di
 * disconnectedCallback dan dipasang lagi di connectedCallback kalau elemen dipasang ulang, jadi elemen
 * yang dilepas dari halaman tidak ditahan oleh apa pun di luar dirinya. Tidak ada listener di window
 * dan tidak ada timer.
 *
 * Java -> JS: setValue(dataUrl), clear(), undo(), setReadOnly(bool), pickImage().
 * JS -> Java: event 'signature-change' dengan detail.value = data URL PNG, atau null bila kosong;
 * event 'signature-error' dengan detail.message untuk gambar unggahan yang ditolak.
 * setValue() sengaja TIDAK memicu event supaya tidak terjadi putaran balik ke server.
 *
 * pickImage() harus dipanggil langsung dari klik tombol di browser (bukan lewat server) supaya dialog file
 * boleh dibuka. File dibaca seluruhnya di browser; ke server hanya terkirim PNG hasil ekspor yang sama
 * dengan hasil menggambar.
 */
class SignaturePadWrapper extends LitElement {

  createRenderRoot() {
    return this;
  }

  render() {
    return html`<canvas style="display:block;width:100%;height:100%;touch-action:none"></canvas>`;
  }

  constructor() {
    super();
    this._pad = null;
    this._canvas = null;
    this._observer = null;
    this._imageDataUrl = null; // gambar tersimpan yang digambar ke kanvas (bukan goresan pad)
    this._imageSize = null; // ukuran asli gambar di atas, di-cache supaya tidak didekode tiap resize
    this._gen = 0;
    this._readOnly = false;
    this._onEndStroke = () => this._emit();
  }

  firstUpdated() {
    this._canvas = this.querySelector('canvas');
    this._pad = new SignaturePad(this._canvas, { penColor: '#111111' });
    this._observer = new ResizeObserver(() => this._resize());
    this._attach();
    this._resize();
  }

  connectedCallback() {
    super.connectedCallback();
    if (this._pad) {
      this._attach();
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this._detach();
  }

  _attach() {
    this._pad.addEventListener('endStroke', this._onEndStroke);
    this._observer.observe(this);
    if (this._readOnly) {
      this._pad.off();
    } else {
      this._pad.on();
    }
  }

  _detach() {
    if (!this._pad) {
      return;
    }
    this._observer.disconnect();
    this._pad.removeEventListener('endStroke', this._onEndStroke);
    this._pad.off();
  }

  // Mengubah ukuran kanvas menghapus isinya (perilaku browser), jadi goresan dan gambar digambar ulang.
  _resize() {
    const w = this.clientWidth;
    const h = this.clientHeight;
    if (!w || !h) {
      return; // sedang tersembunyi (tab/dialog tertutup): biarkan, digambar ulang saat terlihat
    }
    const ratio = pixelRatio();
    const cw = Math.round(w * ratio);
    const ch = Math.round(h * ratio);
    if (this._canvas.width === cw && this._canvas.height === ch) {
      return;
    }
    const strokes = this._pad.toData().slice();
    this._canvas.width = cw;
    this._canvas.height = ch;
    this._canvas.getContext('2d').scale(ratio, ratio);
    this._redraw(strokes);
  }

  // Menggambar ulang gambar tersimpan (diletakkan di tengah, rasio asli dipertahankan) lalu goresan.
  // Async karena gambar perlu didekode; _gen mencegah dua redraw yang tumpang tindih saling menimpa.
  async _redraw(strokes) {
    const gen = ++this._gen;
    this._pad.clear();
    const url = this._imageDataUrl;
    if (url) {
      try {
        if (!this._imageSize || this._imageSize.url !== url) {
          const img = new Image();
          img.src = url;
          await img.decode();
          this._imageSize = { url, w: img.naturalWidth, h: img.naturalHeight };
        }
        if (gen !== this._gen) {
          return;
        }
        const cw = this.clientWidth;
        const ch = this.clientHeight;
        const { w, h } = this._imageSize;
        // Dibatasi 1/rasio (satu piksel gambar = satu piksel kanvas): gambar hasil crop mengisi ukuran aslinya saat
        // digambar dan tidak diperbesar sampai buram atau lebih besar dari yang digambar.
        const s = Math.min(cw / w, ch / h, 1 / pixelRatio());
        await this._pad.fromDataURL(url, {
          ratio: pixelRatio(),
          width: w * s,
          height: h * s,
          xOffset: (cw - w * s) / 2,
          yOffset: (ch - h * s) / 2
        });
      } catch (e) {
        // gambar rusak: abaikan, goresan tetap digambar
      }
      if (gen !== this._gen) {
        return;
      }
    }
    if (strokes.length) {
      this._pad.fromData(strokes, { clear: false });
    }
  }

  // Kotak area bertinta di kanvas (piksel kanvas) ditambah margin, atau seluruh kanvas bila tidak ada tinta.
  _inkBounds() {
    const src = this._canvas;
    const d = src.getContext('2d').getImageData(0, 0, src.width, src.height).data;
    let minX = src.width, minY = src.height, maxX = -1, maxY = -1;
    for (let y = 0; y < src.height; y++) {
      const row = y * src.width * 4;
      for (let x = 0; x < src.width; x++) {
        if (d[row + x * 4 + 3] >= INK_ALPHA_MIN) {
          if (x < minX) minX = x;
          if (x > maxX) maxX = x;
          if (y < minY) minY = y;
          maxY = y;
        }
      }
    }
    if (maxX < 0) {
      return { x: 0, y: 0, w: src.width, h: src.height };
    }
    const pad = Math.round(CROP_PADDING * pixelRatio());
    const x0 = Math.max(0, minX - pad);
    const y0 = Math.max(0, minY - pad);
    return { x: x0, y: y0, w: Math.min(src.width, maxX + 1 + pad) - x0, h: Math.min(src.height, maxY + 1 + pad) - y0 };
  }

  _export() {
    const src = this._canvas;
    const b = this._inkBounds();
    const scale = Math.min(1, MAX_EXPORT_WIDTH / b.w, MAX_EXPORT_HEIGHT / b.h);
    const out = document.createElement('canvas');
    out.width = Math.max(1, Math.round(b.w * scale));
    out.height = Math.max(1, Math.round(b.h * scale));
    out.getContext('2d').drawImage(src, b.x, b.y, b.w, b.h, 0, 0, out.width, out.height);
    const url = out.toDataURL('image/png');
    out.width = 0; // bebaskan memori backing store kanvas sementara
    out.height = 0;
    return url;
  }

  _emit() {
    const empty = this._pad.isEmpty() && !this._imageDataUrl;
    this.dispatchEvent(new CustomEvent('signature-change', {
      detail: { value: empty ? null : this._export() }
    }));
  }

  // ---- dipanggil dari Java ----

  setValue(dataUrl) {
    this._imageDataUrl = dataUrl || null;
    if (this._pad) {
      this._redraw([]);
    } // kalau belum siap, firstUpdated -> _resize menggambar dari _imageDataUrl
  }

  clear() {
    this._imageDataUrl = null;
    this._pad.clear();
    this._emit();
  }

  async undo() {
    const strokes = this._pad.toData().slice(); // salinan, supaya data internal pad tidak ikut berubah
    if (strokes.length) {
      strokes.pop();
      await this._redraw(strokes);
    } else if (this._imageDataUrl) {
      this._imageDataUrl = null;
      this._pad.clear();
    } else {
      return;
    }
    if (this.isConnected) {
      this._emit();
    }
  }

  pickImage() {
    if (this._readOnly) {
      return;
    }
    const input = document.createElement('input'); // sementara, tidak dipasang ke halaman
    input.type = 'file';
    input.accept = 'image/png,image/jpeg';
    input.addEventListener('change', () => {
      const file = input.files && input.files[0];
      if (file) {
        this._loadFile(file);
      }
    }, { once: true });
    input.click();
  }

  _error(message) {
    this.dispatchEvent(new CustomEvent('signature-error', { detail: { message } }));
  }

  // Gambar unggahan MENGGANTIKAN seluruh isi kanvas (goresan dan gambar lama), lalu diekspor seperti hasil menggambar.
  async _loadFile(file) {
    if (!/^image\/(png|jpeg)$/.test(file.type)) {
      this._error('Choose a PNG or JPG file.');
      return;
    }
    if (file.size > MAX_UPLOAD_BYTES) {
      this._error('The image must be 1 MB or smaller.');
      return;
    }
    const url = URL.createObjectURL(file);
    try {
      const img = new Image();
      img.src = url;
      await img.decode();
      const scale = Math.min(MAX_EXPORT_WIDTH / img.naturalWidth, MAX_UPLOAD_HEIGHT / img.naturalHeight, 1);
      const w = Math.max(1, Math.round(img.naturalWidth * scale));
      const h = Math.max(1, Math.round(img.naturalHeight * scale));
      const tmp = document.createElement('canvas');
      tmp.width = w;
      tmp.height = h;
      const ctx = tmp.getContext('2d');
      ctx.drawImage(img, 0, 0, w, h);
      whiteToTransparent(ctx, w, h);
      this._imageDataUrl = tmp.toDataURL('image/png');
      tmp.width = 0; // bebaskan memori backing store kanvas sementara
      tmp.height = 0;
      if (!this.isConnected || !this._pad) {
        return;
      }
      await this._redraw([]);
      this._emit();
    } catch (e) {
      this._error('The image could not be read.');
    } finally {
      URL.revokeObjectURL(url);
    }
  }

  setReadOnly(readOnly) {
    this._readOnly = !!readOnly;
    if (this._pad && this.isConnected) {
      if (this._readOnly) {
        this._pad.off();
      } else {
        this._pad.on();
      }
    }
  }
}

customElements.define('signature-pad-wrapper', SignaturePadWrapper);

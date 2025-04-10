# Quadtree Image Compression

## A. Deskripsi Program

Program ini merupakan implementasi algoritma **kompresi gambar menggunakan Quadtree** berbasis metode **Divide and Conquer**. Gambar dibagi menjadi blok-blok yang lebih kecil, dan setiap blok akan dikompresi jika memiliki tingkat homogenitas warna yang mencukupi. Proses pembagian dilakukan secara rekursif hingga didapatkan blok-blok homogen yang cukup kecil, lalu blok-blok tersebut digabungkan kembali untuk membentuk gambar terkompresi. Selain menghasilkan gambar terkompresi, program juga dapat menghasilkan GIF yang menunjukkan proses kompresi.

---

## B. Requirement Program

- Bahasa Pemrograman: Java (JDK 8 atau lebih baru)
- IDE yang disarankan: IntelliJ IDEA / VSCode dengan plugin Java
- Library tambahan: Tidak ada library eksternal (hanya menggunakan pustaka bawaan Java)
- Direkomendasikan untuk menggunakan sistem dengan memori >2GB untuk gambar besar.

---

## C. Cara Kompilasi Program

1. Buka terminal atau command prompt.
2. Arahkan ke folder root proyek (yang memiliki folder `src/`).
3. Jalankan perintah berikut:

```bash
javac -d bin src/Main.java src/model/*.java src/compression/*.java src/error/*.java src/util/*.java
```

> Perintah ini akan mengompilasi seluruh file Java dan menyimpannya ke dalam folder `bin/`.

---

## D. Cara Menjalankan dan Menggunakan Program

Setelah dikompilasi, jalankan program dengan perintah berikut:

```bash
java -Xmx2G -cp bin Main
```

**Catatan penting:**
- Saat diminta untuk mengisi lokasi file, gunakan **absolute file path**, bukan path relatif.
- Absolute file path adalah path lengkap dari file pada komputer kamu.

**Contoh input path (Linux/Mac):**
```
/home/username/Documents/QuadtreeCompressor/test/raw/flowers.jpg
```

**Contoh input path (Windows):**
```
C:\Users\Username\Documents\QuadtreeCompressor\test\raw\flowers.jpg
```

**Langkah penggunaan:**
1. Masukkan absolute path gambar input.
2. Pilih metode error (1-5).
3. Masukkan threshold.
4. Masukkan ukuran minimum blok (2, 4, 8, dst).
5. Masukkan rasio kompresi target (0 untuk menonaktifkan).
6. Masukkan absolute path untuk output gambar hasil kompresi.
7. Opsional: Masukkan absolute path untuk menyimpan GIF proses kompresi.

---

## E. Author

- **Nama:** Bryan P Hutagalung
  **NIM:** 18222130  
  **Program Studi:** Sistem Teknologi Informasi - ITB  
  **Mata Kuliah:** IF2211 - Strategi Algoritma
  
- **Nama:** Muhammad Farrel Wibowo 
  **NIM:** 13523153  
  **Program Studi:** Teknik Informatika - ITB  
  **Mata Kuliah:** IF2211 - Strategi Algoritma

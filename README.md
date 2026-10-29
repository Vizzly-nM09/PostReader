# PostReader - Aplikasi Android REST API Client

> Tugas Mobile Programming - Bab 9: _Networking with Retrofit_  
> Dosen Pengampu: **Syaeful Anas Aklani, S.Kom., M.Kom.**

---

## 1. Identitas Mahasiswa

| Informasi         | Keterangan                                     |
| :---------------- | :--------------------------------------------- |
| **Nama Lengkap**  | Fadhlan Limanda                                |
| **NPM**           | 2532066                                        |
| **Kelas**         | 3TIMA                                          |
| **Program Studi** | Teknologi Informasi                            |
| **Mata Kuliah**   | Mobile Programming                             |
| **Topik Modul**   | Bab 9: Networking with Retrofit & JSON Parsing |

---

## 2. Kesesuaian Capaian Pembelajaran (Sub-CPMK)

Aplikasi ini dibangun untuk memenuhi capaian pembelajaran pada silabus modul Chapter 9:

| No  | Sub-CPMK         | Keterangan Modul                                                              | Implementasi pada Aplikasi PostReader                                                                                                                                                                         |
| :-: | :--------------- | :---------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
|  1  | **Sub-CPMK 3.5** | Memahami konsep dasar REST API dan implementasi HTTP Client                   | Konfigurasi Retrofit di `ApiClient` & `RetrofitClient`, pendefinisian REST kontrak di `ApiService`, serta implementasi HTTP Methods (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).                             |
|  2  | **Sub-CPMK 3.6** | Mengimplementasikan parsing JSON respons dan menampilkan data ke RecyclerView | Serialisasi dan deserialisasi JSON otomatis menggunakan `GsonConverterFactory`, eksekusi asynchronous via `call.enqueue()`, serta penyajian data melalui `RecyclerView` dengan custom Adapter dan ViewHolder. |

---

## 3. Checklist Kebutuhan Tugas & Fitur

### Persyaratan Dasar Modul
- [x] Dependensi Retrofit 2 dan Gson Converter terpasang pada Gradle
- [x] Izin jaringan (`android.permission.INTERNET` dan `ACCESS_NETWORK_STATE`) di `AndroidManifest.xml`
- [x] Inisialisasi Retrofit client singleton dengan Base URL JSONPlaceholder
- [x] Interface `ApiService` untuk pemanggilan API
- [x] Menampilkan daftar data ke `RecyclerView`
- [x] Navigasi detail item ke `DetailActivity`
- [x] Penanganan status UI (_Loading ProgressBar_, _Empty State_, dan _Error/Failure State_)

### Peningkatan & Fitur Unggulan
- [x] **Operasi CRUD & Partial Update**: Mendukung pembacaan data (`GET`), pembuatan post & tugas (`POST`), pembaruan menyeluruh (`PUT`), pembaruan status parsial (`PATCH`), serta penghapusan (`DELETE`).
- [x] **Penanganan Cerdas Mock Server**: Mengatasi batasan server mock JSONPlaceholder pada penambahan item baru sehingga pembaruan status toggle tugas (`PATCH`) berjalan mulus tanpa kendala HTTP 500.
- [x] **Relasi Data Multi-Resource**:
  - Menampilkan profil penulis postingan (`GET /users/{userId}`) pada kartu detail.
  - Menampilkan daftar komentar pembaca (`GET /posts/{id}/comments`) di bawah artikel.
- [x] **Multi-Tab Resource (Bottom Navigation)**:
  - Tab 1: **Posts** (Daftar artikel blog dan diskusi komentar).
  - Tab 2: **Todos** (Daftar tugas dengan filter chip _Semua_, _Tertunda_, dan _Selesai_).
- [x] **Sinkronisasi Data Lokal (In-Memory Repository)**: Data yang dibuat atau disunting tetap sinkron antar-halaman selama sesi aplikasi berjalan.
- [x] **Pencarian Realtime & Swipe-to-Refresh**: Filter pencarian instan pada judul post/tugas serta penarikan layar untuk memuat ulang data.
- [x] **Aksesibilitas & Sistem Desain Hangat**: Tema visual berbasis palet _Warm Amber & Cream_ yang nyaman dibaca dan memenuhi standar aksesibilitas kontras WCAG AA, dengan target sentuh kartu tugas yang responsif.

---

## 4. Dokumentasi Endpoint API

Base URL yang digunakan: `https://jsonplaceholder.typicode.com/`

| HTTP Method | Endpoint               | Return Type     | Deskripsi                                                         |
| :---------- | :--------------------- | :-------------- | :---------------------------------------------------------------- |
| `GET`       | `/posts`               | `List<Post>`    | Mengambil seluruh daftar postingan artikel (100 item)             |
| `GET`       | `/posts/{id}`          | `Post`          | Mengambil detail satu postingan berdasarkan ID                    |
| `POST`      | `/posts`               | `Post`          | Mengirim data postingan baru ke server                            |
| `PUT`       | `/posts/{id}`          | `Post`          | Memperbarui seluruh data postingan yang ada                       |
| `PATCH`     | `/posts/{id}`          | `Post`          | Memperbarui sebagian konten postingan artikel                     |
| `DELETE`    | `/posts/{id}`          | `Void`          | Menghapus postingan dari server                                   |
| `GET`       | `/users/{id}`          | `User`          | Mengambil data profil penulis (nama, username, email, perusahaan) |
| `GET`       | `/posts/{id}/comments` | `List<Comment>` | Mengambil daftar komentar terkait postingan                       |
| `GET`       | `/todos`               | `List<Todo>`    | Mengambil seluruh daftar tugas (todos)                            |
| `POST`      | `/todos`               | `Todo`          | Menambahkan item tugas baru                                       |
| `PATCH`     | `/todos/{id}`          | `Todo`          | Memperbarui status selesai/belum selesai dari tugas (toggle)      |
| `PUT`       | `/todos/{id}`          | `Todo`          | Memperbarui seluruh data item tugas                               |
| `DELETE`    | `/todos/{id}`          | `Void`          | Menghapus item tugas dari server                                  |

---

## 5. Struktur Direktori Proyek

```text
com.example.postreader/
├── adapter/
│   ├── CommentAdapter.java      # Adapter untuk daftar komentar postingan
│   ├── PostAdapter.java         # Adapter untuk daftar artikel postingan
│   └── TodoAdapter.java         # Adapter untuk daftar tugas dan toggle checkbox
├── data/
│   ├── PostRepository.java      # Pengelola cache dan sinkronisasi CRUD postingan
│   ├── RepositoryCallback.java  # Interface callback generik untuk operasi asinkron
│   └── TodoRepository.java      # Pengelola cache dan status tugas
├── Helpers/
│   └── NetworkUtils.java        # Utilitas pengecekan status konektivitas jaringan
├── Model/
│   ├── Comment.java             # POJO model respons komentar
│   ├── Post.java                # POJO model respons postingan
│   ├── Todo.java                # POJO model respons tugas
│   └── User.java                # POJO model respons data profil pengguna
├── network/
│   ├── ApiClient.java           # Singleton Retrofit builder & OkHttpClient
│   ├── ApiService.java          # Deklarasi antarmuka endpoint Retrofit
│   └── RetrofitClient.java      # Helper penyedia instance ApiService
├── CreateEditPostActivity.java  # Form penambahan dan pengeditan postingan
├── DetailActivity.java          # Layar rincian post, profil penulis, dan komentar
└── MainActivity.java            # Layar utama (daftar post, tab todos, pencarian)
```

---

## 6. Prasyarat & Cara Menjalankan

### Prasyarat Sistem
1. **Android Studio** (Ladybug / Meerkat atau versi yang lebih baru)
2. **Java Development Kit (JDK)**: JDK 17 atau 21 (disarankan menggunakan JBR bawaan Android Studio)
3. **Android SDK**: Min SDK 24 (Android 7.0 Nougat), Target SDK 37
4. **Koneksi Internet** aktif untuk memuat data dari JSONPlaceholder API

### Langkah Menjalankan Aplikasi
1. Buka Android Studio, pilih menu **File > Open**, lalu arahkan ke direktori proyek ini:
   ```text
   PostReader/
   ```
2. Tunggu proses **Gradle Sync** hingga selesai.
3. Hubungkan perangkat fisik Android melalui USB Debugging atau jalankan Android Virtual Device (AVD).
4. Klik tombol **Run 'app'** (`Shift + F10`) di toolbar Android Studio.
5. Anda juga dapat memverifikasi unit test aplikasi melalui terminal:
   ```bash
   ./gradlew testDebugUnitTest
   ```

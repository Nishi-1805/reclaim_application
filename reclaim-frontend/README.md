# Reclaim — Frontend + Backend Image-Upload Patch

## 1. Apply the backend patch

`reclaim-backend-image-upload-patch.zip` contains 6 files, each a drop-in
replacement/addition inside your existing backend project at the same path:

```
src/main/java/com/cdac/controller/ItemImageController.java   (modified — new /upload endpoint)
src/main/java/com/cdac/security/SecurityConfig.java           (modified — permits GET /uploads/**)
src/main/java/com/cdac/security/WebConfig.java                 (new — serves /uploads/** statically)
src/main/java/com/cdac/service/ItemImageService.java           (modified — new interface method)
src/main/java/com/cdac/service/Impl/ItemImageServiceImpl.java  (modified — upload implementation)
src/main/java/com/cdac/util/FileUploadUtil.java                 (modified — was an empty stub)
src/main/resources/application.properties                       (modified — added app.upload.dir)
```

Nothing else in your backend was touched — no controllers, services, entities,
or DTOs beyond these were changed. Copy these 7 files over your existing ones
(same relative paths), then rebuild/restart the Spring Boot app as usual.

New endpoint added:

```
POST /api/items/{itemId}/images/upload   (multipart/form-data, field name: "file")
```

It validates file type (jpg/jpeg/png/webp/gif) and size (max 5MB), stores the
file under `uploads/items/{itemId}/` relative to wherever the app is run from,
and returns an `ItemImageResponse` whose `imageUrl` looks like
`/uploads/items/12/3f2c1a-photo.jpg`. That path is served back over HTTP by the
new static resource mapping, so the frontend can load it directly as
`http://localhost:8080/uploads/items/12/3f2c1a-photo.jpg`.

## 2. Run the frontend

```bash
cd reclaim-frontend
npm install
npm run dev
```

The dev server runs on `http://localhost:5173` and points at
`http://localhost:8080/api` by default (see `.env` → `VITE_API_BASE_URL`).
Change that value if your backend runs on a different host/port.

## 3. What's built so far

- Full API layer for every backend module (auth, items, matches, claims,
  notifications, users) — see `src/api/`
- Auth (JWT session in `AuthContext`, protected routes, admin-only route guard)
- Dashboard page — fully wired to live data
- Sidebar / Topbar / responsive layout shell matching the dark-theme mockup
- Placeholder pages for Items, Matches, Claims, Notifications, Profile and
  Admin Users — routed and ready, to be built out in the next stages

## 4. Login

Use the credentials of a user already registered via your backend (or use the
Register page in the app) to log in. Admin-only screens (Users) only appear in
the sidebar for users whose `role` is `ADMIN`.

# CI/CD bələdçisi (GitHub Actions)

Bu sənəd **Email Delivery** ekosistemində avtomatik build, push və deploy-un necə işlədiyini izah edir.

**Bu layihə:** `email-delivery` (Backend)  
**Workflow:** `.github/workflows/deploy-backend.yml` — **Build and Deploy Email Delivery Backend**

---

## Qısa icmal: nə baş verir?

1. Siz **tag** push edirsiniz (məs. `v1.0.0.1`) və ya Actions-dan manual run edirsiniz.
2. GitHub runner **Docker image** build edir və **Docker Hub**-a push edir.
3. Runner **SSH** ilə serverə qoşulur (`/root/email-delivery`).
4. Serverdə `.env` faylında **`BACKEND_IMAGE`** yenilənir.
5. `docker compose pull backend` + `docker compose up -d backend` işləyir.

**Sizin manual addımlarınız artıq lazım deyil:** lokal build, `.env` edit, SSH, `docker compose up`.

---

## Layihələr və deploy hədəfləri

| Layihə | GitHub repo | Image adı | `.env` açarı | Compose servisi | Port |
|--------|-------------|-----------|--------------|-----------------|------|
| Backend | `email-delivery` | `email-delivery-backend` | `BACKEND_IMAGE` | `backend` | 8092 |
| Frontend | `email-delivery-ui` | `email-delivery-frontend` | `FRONTEND_IMAGE` | `frontend` | 8093 |

Tam image formatı: `ingressgroup/<image-adı>:<tag>`  
Məsələn: `ingressgroup/email-delivery-backend:v1.0.0.1`

---

## GitHub Secrets (hər repo-da)

**Settings → Secrets and variables → Actions**

| Secret | Nədir | Nümunə |
|--------|-------|--------|
| `DOCKERHUB_USERNAME` | Docker Hub user | `ingressgroup` |
| `DOCKERHUB_TOKEN` | Docker Hub access token | (token) |
| `DEPLOY_HOST` | Server IP / host | `65.21.51.215` |
| `DEPLOY_PORT` | SSH port | `2222` |
| `DEPLOY_USER` | SSH user | `test` |
| `DEPLOY_PASSWORD` | SSH parol | (parol) |
| `DEPLOY_PATH` | Serverdə compose + `.env` qovluğu | `/root/email-delivery` |

**Yalnız `email-delivery-ui` üçün əlavə (opsional):**

| Secret | Default |
|--------|---------|
| `FRONTEND_API_URL` | `http://65.21.51.215:8092/admin/v1` |

---

## Deploy necə edilir?

### Tag ilə (tövsiyə)

```bash
git tag v1.0.0.1
git push origin main --tags
```

Tag **`v` ilə başlamalıdır** (məs. `v1.0.0.1`).

### Manual (Actions)

GitHub → **Actions** → workflow adı → **Run workflow** → tag yazın.

### Prosesi izləmək

GitHub → **Actions** → son run → addım logları.

Serverdə yoxlama:

```bash
ssh -p 2222 test@65.21.51.215 'grep BACKEND_IMAGE /root/email-delivery/.env && docker ps --filter name=email-delivery-backend'
curl -s http://65.21.51.215:8092/actuator/health
```

---

## Server bir dəfəlik hazırlıq

Email Delivery PMS-dən **ayrı** standalone layihədir. Serverdə ayrıca qovluq lazımdır:

```bash
mkdir -p /root/email-delivery
```

Bu qovluğa köçürün:

- `docker-compose.prod.yml` (`email-delivery` repo-dan)
- `.env` (`.env.prod.example` əsasında doldurulmuş)

İlk dəfə stack-i qaldırmaq:

```bash
cd /root/email-delivery
docker compose -f docker-compose.prod.yml up -d
```

### Server deploy user icazəsi (əl ilə) — vacib

GitHub Actions SSH ilə `DEPLOY_USER` (məs. `test`) qoşulur və `.env`-də `sed -i` ilə `BACKEND_IMAGE` / `FRONTEND_IMAGE` yeniləyir. **PMS `/root/hotel` üçün etdiyiniz `setfacl` burada `/root/email-delivery` üçündür.**

Ətraflı addım-addım: **[SERVER-DEPLOY-SETUP.md](./SERVER-DEPLOY-SETUP.md)**

#### 1. Serverə root kimi daxil olun

```bash
ssh -p 2222 root@65.21.51.215
```

#### 2. Qovluğu yoxlayın / yaradın

```bash
mkdir -p /root/email-delivery
ls -la /root/email-delivery
```

`DEPLOY_PATH` secret fərqlidirsə, həmin yolu istifadə edin.

#### 3. Deploy user — docker qrupu

```bash
usermod -aG docker test
```

#### 4. Qovluq icazəsi (setfacl)

```bash
setfacl -R -m u:test:rwx /root/email-delivery
setfacl -R -d -m u:test:rwx /root/email-delivery
```

`setfacl` yoxdursa:

```bash
chown -R root:test /root/email-delivery
chmod -R g+rwX /root/email-delivery
```

#### 5. `.env` yazıla bilən olsun

```bash
chmod 664 /root/email-delivery/.env
```

#### 6. Skript ilə (alternativ)

```bash
sudo DEPLOY_USER=test DEPLOY_PATH=/root/email-delivery bash scripts/server-deploy-setup.sh
```

#### 7. Yoxlama

```bash
sudo -u test test -w /root/email-delivery && echo "qovluq OK"
sudo -u test test -w /root/email-delivery/.env && echo ".env OK"
sudo -u test docker compose version
```

`setfacl` olmadan xəta: `sed: couldn't open temporary file ... Permission denied`

---

## Deploy ardıcıllığı

1. **Backend** (`email-delivery`) — tag push → `BACKEND_IMAGE` yenilənir
2. **Frontend** (`email-delivery-ui`) — `FRONTEND_API_URL` secret backend-ə işarə etməlidir, sonra tag push

Frontend build zamanı `VITE_ADMIN_API_URL` image-ə yazılır — `.env`-də UI URL dəyişmir.

---

## Server `.env` (bir dəfəlik)

```env
BACKEND_IMAGE=ingressgroup/email-delivery-backend:v1.0.0.1
FRONTEND_IMAGE=ingressgroup/email-delivery-frontend:v1.0.0.1

DB_ROOT_PASSWORD=<güclü-parol>
DB_NAME=email_delivery
APP_SECRET_KEY=<jwt-secret>

ADMIN_BOOTSTRAP_ENABLED=true
ADMIN_BOOTSTRAP_EMAIL=admin@example.com
ADMIN_BOOTSTRAP_PASSWORD=<admin-parol>
ADMIN_PUBLIC_BASE_URL=http://65.21.51.215:8092
ADMIN_CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:*,http://127.0.0.1:*,http://65.21.51.215:8093

MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
ADMIN_MAIL_FROM=noreply@example.com

VITE_ADMIN_API_URL=http://65.21.51.215:8092/admin/v1
```

Pipeline hər deploy-da yalnız `BACKEND_IMAGE` / `FRONTEND_IMAGE` sətirlərini avtomatik yeniləyir.

---

## Server dəyişəndə nə etməlisiniz?

### 1. GitHub Secrets (hər repo-da yeniləyin)

| Dəyişən | Secret |
|---------|--------|
| Yeni IP | `DEPLOY_HOST` |
| Yeni SSH port | `DEPLOY_PORT` |
| Yeni SSH user | `DEPLOY_USER` |
| Yeni parol | `DEPLOY_PASSWORD` |
| Yeni qovluq | `DEPLOY_PATH` |

**Kod dəyişməyə ehtiyac yoxdur** — yalnız Secrets kifayətdir.

### 2. Frontend API URL dəyişirsə

`email-delivery-ui` repo-da `FRONTEND_API_URL` secret yeniləyin və frontend-i yenidən tag ilə deploy edin.

---

## Image adı və ya Docker Hub repo dəyişəndə

| # | Fayl | Nə dəyişir |
|---|------|------------|
| 1 | `.github/workflows/deploy-*.yml` | `env: DOCKER_REPO` və `IMAGE_NAME` |
| 2 | `scripts/build-and-push.sh` | Image adı sətri |
| 3 | `docker-compose.prod.yml` | Default image fallback |

**Bu layihə (`email-delivery`) üçün:**

- Workflow: `IMAGE_NAME: email-delivery-backend`
- Skript: `email-delivery-backend:${TAG}`
- `.env` açarı: `BACKEND_IMAGE`

---

## Tez-tez rast gəlinən xətalar

| Xəta | Həll |
|------|------|
| `sed: couldn't open temporary file ... Permission denied` | [SERVER-DEPLOY-SETUP.md](./SERVER-DEPLOY-SETUP.md) — `setfacl` |
| `Permission denied` `/root/email-delivery` | [SERVER-DEPLOY-SETUP.md](./SERVER-DEPLOY-SETUP.md) — deploy user icazəsi |
| `docker compose` icazə | `usermod -aG docker test` |
| `DEPLOY_PATH not found` | Secret: `/root/email-delivery` |
| Tag işləmir | Tag `v` ilə başlamalıdır |
| Frontend köhnə API | `FRONTEND_API_URL` secret + yenidən deploy |
| Backend DB xətası | `.env`: `DB_ROOT_PASSWORD`, `DB_NAME` |
| CORS xətası | `.env`: `ADMIN_CORS_ALLOWED_ORIGIN_PATTERNS` frontend URL daxil olmalıdır |

---

## Layihə workflow adları (Actions-da)

| Repo | Workflow adı |
|------|----------------|
| `email-delivery` | Build and Deploy Email Delivery Backend |
| `email-delivery-ui` | Build and Deploy Email Delivery Frontend |

## Bu layihəyə xas fayllar

| Fayl | Məqsəd |
|------|--------|
| `.github/workflows/deploy-backend.yml` | CI/CD workflow |
| `scripts/server-deploy-setup.sh` | Serverdə deploy user icazəsi (setfacl + docker) |
| `docs/SERVER-DEPLOY-SETUP.md` | Server icazəsi — əl ilə addımlar |
| `scripts/build-and-push.sh` | CI-də image build/push |
| `docker-compose.prod.yml` | Prod stack (serverdə `/root/email-delivery`) |
| `docker-compose.yml` | Local DB + Adminer |
| `Dockerfile` | Backend image quruluşu |

Frontend bələdçisi: `email-delivery-ui/docs/CI-CD-GUIDE.md`

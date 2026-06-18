# Server deploy icazəsi (əl ilə)

GitHub Actions deploy zamanı SSH ilə `DEPLOY_USER` (məs. `test`) serverə qoşulur və `/root/email-delivery/.env` faylında `sed -i` ilə image tag yeniləyir. Deploy user-in qovluğa **yazma** icazəsi olmalıdır — PMS-də `/root/hotel` üçün etdiyiniz `setfacl` ilə eyni prinsip.

**Xəta (icazə yoxdursa):**

```
sed: couldn't open temporary file ... Permission denied
```

---

## 1. Serverə root kimi daxil olun

```bash
ssh -p 2222 root@65.21.51.215
```

Öz `DEPLOY_HOST`, `DEPLOY_PORT` və user-inizi istifadə edin. İcazəni **root** verir; deploy `test` (və ya `DEPLOY_USER`) ilə işləyir.

---

## 2. Deploy qovluğunu yoxlayın

```bash
ls -la /root/email-delivery
```

Yoxdursa:

```bash
mkdir -p /root/email-delivery
```

`DEPLOY_PATH` secret başqa qovluqdursa (məs. `/home/test/email-delivery`), aşağıdakı əmrlərdə həmin yolu yazın.

Bu qovluqda olmalıdır:

- `docker-compose.prod.yml` (`email-delivery` repo-dan)
- `.env` (`.env.prod.example` əsasında)

```bash
cp .env.prod.example .env   # ilk dəfə
nano .env                   # parollar, URL-lər
```

---

## 3. Deploy user-i docker qrupuna əlavə edin

```bash
usermod -aG docker test
```

`test` əvəzinə GitHub `DEPLOY_USER` secret-ındakı istifadəçi adı.

---

## 4. Qovluq icazəsi (setfacl — PMS ilə eyni)

```bash
setfacl -R -m u:test:rwx /root/email-delivery
setfacl -R -d -m u:test:rwx /root/email-delivery
```

| Əmr | Məqsəd |
|-----|--------|
| Birinci sətir | Mövcud fayl və qovluqlara icazə |
| İkinci sətir | Gələcəkdə yaradılan fayllar üçün default icazə |

`setfacl` yoxdursa:

```bash
chown -R root:test /root/email-delivery
chmod -R g+rwX /root/email-delivery
```

---

## 5. `.env` faylını yazıla bilən edin

```bash
chmod 664 /root/email-delivery/.env
```

---

## 6. Skript ilə (alternativ)

`email-delivery` repo-dan:

```bash
sudo DEPLOY_USER=test DEPLOY_PATH=/root/email-delivery bash scripts/server-deploy-setup.sh
```

---

## 7. Yoxlama

Root-dan:

```bash
sudo -u test test -w /root/email-delivery && echo "qovluq yazıla bilir"
sudo -u test test -w /root/email-delivery/.env && echo ".env yazıla bilir"
sudo -u test docker compose version
```

`test` user ilə SSH:

```bash
exit
ssh -p 2222 test@65.21.51.215
cd /root/email-delivery
grep BACKEND_IMAGE .env
grep FRONTEND_IMAGE .env
```

---

## 8. GitHub Actions-u yenidən işə salın

Actions → uğursuz workflow → **Re-run jobs**, və ya tag-i yenidən push edin.

---

## Tez-tez xətalar

| Xəta | Həll |
|------|------|
| `sed: couldn't open temporary file ... Permission denied` | `setfacl` (4-cü addım) |
| `Permission denied` qovluq | `setfacl` və ya `chown`/`chmod` |
| `docker compose` icazə | `usermod -aG docker test` |
| `DEPLOY_PATH not found` | GitHub Secret `DEPLOY_PATH` düzgün qovluq |

---

Əlaqəli sənədlər:

- `email-delivery/docs/CI-CD-GUIDE.md` — backend CI/CD
- `email-delivery-ui/docs/CI-CD-GUIDE.md` — frontend CI/CD

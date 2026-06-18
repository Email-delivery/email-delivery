#!/usr/bin/env bash
# Serverdə bir dəfə root kimi işlədin (PMS /root/hotel ilə eyni prinsip).
#
#   sudo bash scripts/server-deploy-setup.sh
#
# Opsional:
#   DEPLOY_USER=test DEPLOY_PATH=/root/email-delivery sudo -E bash scripts/server-deploy-setup.sh

set -euo pipefail

DEPLOY_USER="${DEPLOY_USER:-test}"
DEPLOY_PATH="${DEPLOY_PATH:-/root/email-delivery}"

if [[ "${EUID:-$(id -u)}" -ne 0 ]]; then
  echo "Run as root: sudo bash $0" >&2
  exit 1
fi

if ! id "$DEPLOY_USER" &>/dev/null; then
  echo "User not found: $DEPLOY_USER" >&2
  exit 1
fi

mkdir -p "$DEPLOY_PATH"

echo "Deploy user: $DEPLOY_USER"
echo "Deploy path: $DEPLOY_PATH"

# Docker (compose pull/up)
if ! groups "$DEPLOY_USER" | grep -q '\bdocker\b'; then
  usermod -aG docker "$DEPLOY_USER"
  echo "Added $DEPLOY_USER to docker group (re-login may be required)."
else
  echo "$DEPLOY_USER already in docker group."
fi

# Qovluq + .env üzərində yazma (sed -i, compose) — PMS setfacl
if command -v setfacl &>/dev/null; then
  setfacl -R -m "u:${DEPLOY_USER}:rwx" "$DEPLOY_PATH"
  setfacl -R -d -m "u:${DEPLOY_USER}:rwx" "$DEPLOY_PATH"
  echo "ACL set on $DEPLOY_PATH for $DEPLOY_USER."
else
  echo "setfacl not found; falling back to chown/chmod."
  chown -R "root:${DEPLOY_USER}" "$DEPLOY_PATH"
  chmod -R g+rwX "$DEPLOY_PATH"
fi

ENV_FILE="${DEPLOY_PATH}/.env"
if [[ -f "$ENV_FILE" ]]; then
  chmod 664 "$ENV_FILE"
  echo "Ensured writable .env: $ENV_FILE"
fi

echo ""
echo "Done. Verify as deploy user:"
echo "  sudo -u $DEPLOY_USER test -w $DEPLOY_PATH && echo 'directory writable'"
echo "  sudo -u $DEPLOY_USER docker compose version"

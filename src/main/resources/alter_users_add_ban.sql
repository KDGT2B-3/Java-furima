-- 既存の users テーブルに BAN/有効列を追加する移行スクリプト
-- アカウント停止機能・BAN 履歴管理のための列を後付けで追加する DDL
ALTER TABLE public.users
ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE, ADD COLUMN IF NOT EXISTS banned BOOLEAN NOT NULL DEFAULT FALSE, ADD COLUMN IF NOT EXISTS ban_reason TEXT, ADD COLUMN IF NOT EXISTS banned_at TIMESTAMP, ADD COLUMN IF NOT EXISTS banned_by_admin_id INT; -- ログイン可否制御
-- BAN フラグ
-- BAN 理由
-- BAN 日時
-- 処理を行った管理者 ID
-- BAN 機能の検索効率改善用インデックス
CREATE INDEX IF NOT EXISTS idx_users_banned -- BAN 済みユーザー抽出用
CREATE INDEX IF NOT EXISTS idx_users_banned_by -- 管理者別 BAN 履歴検索用
ON public.users(banned);
ON public.users(banned_by_admin_id);
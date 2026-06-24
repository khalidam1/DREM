-- تفعيل إضافة pgcrypto إذا لزم الأمر
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. جدول المستخدمين (نظام تسجيل الدخول والأدمن المحلي)
CREATE TABLE public.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. جدول الإعلانات والإعدادات (للأدمن)
CREATE TABLE public.ads_config (
    id INTEGER PRIMARY KEY DEFAULT 1,
    ads_enabled BOOLEAN NOT NULL DEFAULT true,
    global_ad_code TEXT,
    home_ad_code TEXT,
    player_ad_code TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. جدول المحتوى (مسلسلات وأفلام)
CREATE TABLE public.dramas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT NOT NULL,
    is_movie BOOLEAN NOT NULL DEFAULT true,
    movie_embed_code TEXT,
    image_url TEXT NOT NULL,
    views INTEGER DEFAULT 0,
    likes INTEGER DEFAULT 0,
    badge TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. جدول الحلقات للمسلسلات
CREATE TABLE public.episodes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    drama_id UUID REFERENCES public.dramas(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    embed_code TEXT NOT NULL,
    episode_number INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. جدول التعليقات (يتيح التعليق باستخدام معرّف الجهاز أو اسم المستخدم)
CREATE TABLE public.comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    drama_id UUID REFERENCES public.dramas(id) ON DELETE CASCADE,
    identifier TEXT NOT NULL, -- Device ID أو Username
    user_name TEXT NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. جدول الإعجابات (يمنع تكرار الإعجاب من نفس الجهاز أو الحساب لكل فيلم/مسلسل)
CREATE TABLE public.user_likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    drama_id UUID REFERENCES public.dramas(id) ON DELETE CASCADE,
    identifier TEXT NOT NULL, -- Device ID أو Username
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(drama_id, identifier)
);

-- 7. جدول المفضلة (لمزامنة المفضلة أو حفظها بناءً على معرّف)
CREATE TABLE public.favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    drama_id UUID REFERENCES public.dramas(id) ON DELETE CASCADE,
    identifier TEXT NOT NULL, -- Device ID أو Username
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(drama_id, identifier)
);

-- ==========================================
-- إعدادات الأمان RLS (Row Level Security)
-- ==========================================
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ads_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.dramas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.episodes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;

-- ======== السياسات (Policies) ========
-- السماح للجميع بقراءة البيانات العامة
CREATE POLICY "Allow public read on users" ON public.users FOR SELECT USING (true);
CREATE POLICY "Allow public read on ads" ON public.ads_config FOR SELECT USING (true);
CREATE POLICY "Allow public read on dramas" ON public.dramas FOR SELECT USING (true);
CREATE POLICY "Allow public read on episodes" ON public.episodes FOR SELECT USING (true);
CREATE POLICY "Allow public read on comments" ON public.comments FOR SELECT USING (true);
CREATE POLICY "Allow public read on likes" ON public.user_likes FOR SELECT USING (true);
CREATE POLICY "Allow public read on favorites" ON public.favorites FOR SELECT USING (true);

-- السماح للزوار أو المستخدمين بالإضافة والتعديل في التعليقات والإعجابات والمفضلة (يمكن الاعتماد على Device ID)
CREATE POLICY "Allow public insert on comments" ON public.comments FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public update/delete on comments" ON public.comments FOR ALL USING (true);

CREATE POLICY "Allow public insert on likes" ON public.user_likes FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public delete on likes" ON public.user_likes FOR DELETE USING (true);

CREATE POLICY "Allow public insert on favorites" ON public.favorites FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow public delete on favorites" ON public.favorites FOR DELETE USING (true);

-- (ملاحظة: لكي يتحكم الأدمن بالمحتوى بدون Supabase Auth بل من داخل التطبيق، نحتاج صلاحية التعديل للحالات أو نستخدم مفتاح Service Key في التطبيق)
CREATE POLICY "Allow public full access on dramas" ON public.dramas FOR ALL USING (true);
CREATE POLICY "Allow public full access on episodes" ON public.episodes FOR ALL USING (true);
CREATE POLICY "Allow public full access on ads_config" ON public.ads_config FOR ALL USING (true);
CREATE POLICY "Allow public full access on users" ON public.users FOR ALL USING (true);

-- ==========================================
-- الدوال (Functions) والمشغلات (Triggers)
-- ==========================================

-- دالة لزيادة المشاهدات بسلاسة
CREATE OR REPLACE FUNCTION increment_views(drama_id_input UUID)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
  UPDATE public.dramas
  SET views = views + 1
  WHERE id = drama_id_input;
END;
$$;

-- دالة وتريجر لتحديث حقل الإعجابات في جدول dramas تلقائياً
CREATE OR REPLACE FUNCTION update_likes_count()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    UPDATE public.dramas SET likes = likes + 1 WHERE id = NEW.drama_id;
    RETURN NEW;
  ELSIF TG_OP = 'DELETE' THEN
    UPDATE public.dramas SET likes = likes - 1 WHERE id = OLD.drama_id;
    RETURN OLD;
  END IF;
  RETURN NULL;
END;
$$;

CREATE TRIGGER trigger_update_likes_count
AFTER INSERT OR DELETE ON public.user_likes
FOR EACH ROW EXECUTE FUNCTION update_likes_count();


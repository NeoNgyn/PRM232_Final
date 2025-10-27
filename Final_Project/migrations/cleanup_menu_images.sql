-- Script to update legacy image URLs in Menu table

-- 1. Check current image URLs
SELECT menu_id, menu_name, image_url,
       CASE
           WHEN image_url LIKE 'http%' THEN 'Valid URL'
           WHEN image_url LIKE 'content://%' THEN 'Content URI'
           WHEN image_url LIKE 'file://%' THEN 'File URI'
           WHEN image_url IS NULL OR image_url = '' THEN 'No Image'
           ELSE 'Legacy Filename (Invalid)'
       END AS image_status
FROM Menu;

-- 2. Clear legacy local filenames (like 'diet_menu.jpg', 'healthy_menu.jpg')
-- These cannot be loaded and should be replaced with empty string
UPDATE Menu
SET image_url = '',
    update_at = NOW()
WHERE image_url NOT LIKE 'http%'
  AND image_url NOT LIKE 'content://%'
  AND image_url NOT LIKE 'file://%'
  AND image_url IS NOT NULL
  AND image_url != '';

-- 3. Verify the update
SELECT menu_id, menu_name, image_url,
       CASE
           WHEN image_url LIKE 'http%' THEN 'Valid URL'
           WHEN image_url LIKE 'content://%' THEN 'Content URI'
           WHEN image_url LIKE 'file://%' THEN 'File URI'
           WHEN image_url IS NULL OR image_url = '' THEN 'No Image'
           ELSE 'Legacy Filename (Invalid)'
       END AS image_status
FROM Menu;

-- 4. List menus that need new images
SELECT menu_id, menu_name, description
FROM Menu
WHERE image_url IS NULL OR image_url = ''
ORDER BY menu_id;


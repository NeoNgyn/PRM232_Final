-- migrations/normalize_menu_ids.sql
-- Migration script for MySQL to normalize Menu.menu_id to format M### (e.g. M001)
-- IMPORTANT: backup your DB before running any statements in this file.
-- Usage (CLI):
--   mysqldump -u USER -p DB_NAME Menu RecipeInMenu > backup_menu_recipeinmenu.sql
--   mysql -u USER -p DB_NAME < migrations/normalize_menu_ids.sql
-- Or run the blocks below step-by-step in MySQL Workbench.

-- 1) PREVIEW: show menu rows that are NOT in format M followed by exactly 3 digits
SELECT menu_id, menu_name, image_url, create_at
FROM Menu
WHERE NOT (menu_id REGEXP '^M[0-9]{3}$')
ORDER BY create_at ASC;

-- 2) Preview count
SELECT COUNT(*) AS bad_count FROM Menu WHERE NOT (menu_id REGEXP '^M[0-9]{3}$');

-- 3) Determine the starting index for new ids (max existing numeric suffix among valid ids)
SELECT IFNULL(MAX(CAST(SUBSTRING(menu_id,2) AS UNSIGNED)), 0) AS max_valid_num
FROM Menu
WHERE menu_id REGEXP '^M[0-9]{1,}$';

-- 4) Create temporary mapping table (preview only)
DROP TABLE IF EXISTS tmp_menu_map;
CREATE TEMPORARY TABLE tmp_menu_map (
  old_id VARCHAR(128) PRIMARY KEY,
  new_id VARCHAR(128) NOT NULL
) ENGINE=Memory;

-- 5) Populate mapping: assign new sequential M### values to invalid ids, ordered by create_at
SET @start = (
  SELECT IFNULL(MAX(CAST(SUBSTRING(menu_id,2) AS UNSIGNED)), 0)
  FROM Menu
  WHERE menu_id REGEXP '^M[0-9]{1,}$'
);

INSERT INTO tmp_menu_map (old_id, new_id)
SELECT menu_id,
       CONCAT('M', LPAD(@start := @start + 1, 3, '0'))
FROM Menu
WHERE NOT (menu_id REGEXP '^M[0-9]{3}$')
ORDER BY create_at ASC;

-- 6) Preview mapping
SELECT * FROM tmp_menu_map;

-- 7) Safety checks: ensure no new_id conflicts with existing (excluding the rows that will be updated)
SELECT m.new_id, COUNT(*) AS cnt
FROM tmp_menu_map m
LEFT JOIN Menu orig ON orig.menu_id = m.new_id
GROUP BY m.new_id
HAVING cnt > 0;

-- If the previous query returns rows, there is a conflict: inspect results and resolve manually.

-- 8) Preview how many RecipeInMenu rows will be updated
SELECT COUNT(*) AS refs_to_update
FROM RecipeInMenu rim
JOIN tmp_menu_map m ON rim.menu_id = m.old_id;

-- ==========================
-- APPLY CHANGES (run only after you reviewed previews)
-- ==========================
-- Wrap inside transaction and disable FK checks temporarily to avoid FK constraint errors.
SET FOREIGN_KEY_CHECKS=0;

-- Update referencing table(s) first
UPDATE RecipeInMenu rim
JOIN tmp_menu_map m ON rim.menu_id = m.old_id
SET rim.menu_id = m.new_id;

-- Update Menu table
UPDATE Menu t
JOIN tmp_menu_map m ON t.menu_id = m.old_id
SET t.menu_id = m.new_id;

SET FOREIGN_KEY_CHECKS=1;

-- 9) Final checks
SELECT * FROM Menu WHERE menu_id REGEXP '^M[0-9]{3}$' ORDER BY menu_id DESC LIMIT 50;
SELECT * FROM RecipeInMenu ORDER BY recipeMenu_id DESC LIMIT 50;

-- 10) Drop temporary table (if not TEMPORARY)
DROP TEMPORARY TABLE IF EXISTS tmp_menu_map;

-- ROLLBACK notes:
-- If you need to rollback after applying, restore from your backup created in step 0.
-- Example restore (CLI):
--   mysql -u USER -p DB_NAME < backup_menu_recipeinmenu.sql

-- End of migration script


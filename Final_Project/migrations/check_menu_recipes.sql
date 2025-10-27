-- Script to check and fix menu-recipe relationships

-- 1. Check all menus
SELECT menu_id, menu_name, user_id FROM Menu;

-- 2. Check all recipes
SELECT recipe_id, name, user_id FROM Recipe;

-- 3. Check RecipeInMenu table (linking table)
SELECT * FROM RecipeInMenu;

-- 4. Check if menu M001 has any recipes
SELECT r.recipe_id, r.name, rim.menu_id
FROM Recipe r
INNER JOIN RecipeInMenu rim ON r.recipe_id = rim.recipe_id
WHERE rim.menu_id = 'M001';

-- 5. If no recipes found, insert sample data
-- First, make sure we have recipes (already inserted in previous scripts)
INSERT INTO Recipe (recipe_id, name, instruction, nutrition, image_url, create_at, update_at, user_id)
VALUES
    ('R001', 'Cơm gà xối mỡ', 'Chiên gà, nấu cơm, chan mỡ gà lên cơm.', 'Cung cấp protein và tinh bột.', 'com_ga.jpg', NOW(), NOW(), 'U001'),
    ('R002', 'Salad cá hồi', 'Trộn cá hồi với rau củ và nước sốt.', 'Giàu Omega-3 và vitamin.', 'salad_salmon.jpg', NOW(), NOW(), 'U002'),
    ('R003', 'Súp cà rốt', 'Nấu cà rốt với nước dùng và gia vị.', 'Giàu vitamin A.', 'soup_carrot.jpg', NOW(), NOW(), 'U003')
ON DUPLICATE KEY UPDATE update_at = NOW();

-- 6. Link recipes to menu M001
INSERT INTO RecipeInMenu (recipeMenu_id, recipe_id, menu_id)
VALUES
    ('RM01', 'R001', 'M001'),
    ('RM02', 'R002', 'M001')
ON DUPLICATE KEY UPDATE recipe_id = VALUES(recipe_id);

-- 7. Link recipes to menu M002
INSERT INTO RecipeInMenu (recipeMenu_id, recipe_id, menu_id)
VALUES
    ('RM03', 'R003', 'M002')
ON DUPLICATE KEY UPDATE recipe_id = VALUES(recipe_id);

-- 8. Verify the links
SELECT
    m.menu_id,
    m.menu_name,
    r.recipe_id,
    r.name as recipe_name,
    rim.recipeMenu_id
FROM Menu m
LEFT JOIN RecipeInMenu rim ON m.menu_id = rim.menu_id
LEFT JOIN Recipe r ON rim.recipe_id = r.recipe_id
ORDER BY m.menu_id, r.recipe_id;

-- 9. Count recipes per menu
SELECT
    m.menu_id,
    m.menu_name,
    COUNT(rim.recipe_id) as recipe_count
FROM Menu m
LEFT JOIN RecipeInMenu rim ON m.menu_id = rim.menu_id
GROUP BY m.menu_id, m.menu_name
ORDER BY m.menu_id;


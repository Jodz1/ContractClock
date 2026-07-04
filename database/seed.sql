USE time_tracker_db;

INSERT INTO clients (name, email, company_name, notes)
VALUES ('Hi-Fish Fashion', 'client@example.com', 'Hi-Fish Fashion', 'Seed client for Recka')
ON DUPLICATE KEY UPDATE name = name;

INSERT INTO contracts (client_id, title, description, status, current_hourly_rate, currency_code)
SELECT c.id,
       'Shopify Collection Implementation & Store Support',
       'ABGRUND collection work, Shopify theme changes, support and layout fixes.',
       'ACTIVE',
       15.00,
       'USD'
FROM clients c
WHERE c.name = 'Hi-Fish Fashion'
  AND NOT EXISTS (
    SELECT 1 FROM contracts x
    WHERE x.client_id = c.id
      AND x.title = 'Shopify Collection Implementation & Store Support'
  );

INSERT INTO contract_rates (contract_id, hourly_rate, currency_code, valid_from, note)
SELECT ct.id, 15.00, 'USD', NOW(), 'Initial seed rate'
FROM contracts ct
WHERE ct.title = 'Shopify Collection Implementation & Store Support'
  AND NOT EXISTS (SELECT 1 FROM contract_rates r WHERE r.contract_id = ct.id);

INSERT IGNORE INTO activity_tags (name, color_hex) VALUES
  ('Development', '#10B981'),
  ('Design', '#6366F1'),
  ('Support', '#F59E0B'),
  ('Meeting', '#64748B');

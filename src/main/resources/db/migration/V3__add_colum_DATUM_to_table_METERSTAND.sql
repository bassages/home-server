ALTER TABLE meterstand ADD date DATE;
UPDATE meterstand SET date = DATE_TRUNC(DAY, date_time);
ALTER TABLE meterstand ALTER COLUMN date SET NOT NULL;

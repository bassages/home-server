ALTER TABLE opgenomen_vermogen ADD datum DATE;
UPDATE opgenomen_vermogen SET datum = DATE_TRUNC(DAY, datumtijd);
ALTER TABLE opgenomen_vermogen ALTER COLUMN datumtijd SET NOT NULL;

ALTER TABLE meterstand ADD meter_id_electricity VARCHAR(20);
ALTER TABLE meterstand ADD meter_id_gas VARCHAR(20);

UPDATE meterstand
   SET meter_id_electricity = 'UNKNOWN_E_20260706',
       meter_id_gas = 'UNKNOWN_G_20260706';

ALTER TABLE meterstand ALTER COLUMN meter_id_electricity SET NOT NULL;
ALTER TABLE meterstand ALTER COLUMN meter_id_gas SET NOT NULL;

CREATE INDEX idx_meterstand_meter_id_electricity_date_time
    ON meterstand (meter_id_electricity, date_time);

CREATE INDEX idx_meterstand_meter_id_gas_date_time
    ON meterstand (meter_id_gas, date_time);


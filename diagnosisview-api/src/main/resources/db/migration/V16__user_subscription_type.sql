ALTER TABLE dv_user
    ADD COLUMN subscription_type VARCHAR(100) DEFAULT NULL,
    ADD COLUMN subscription_data jsonb DEFAULT NULL;

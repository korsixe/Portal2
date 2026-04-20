DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM pg_constraint c
    JOIN pg_class t ON c.conrelid = t.oid
    WHERE t.relname = 'ads' AND c.conname = 'ads_status_check'
  ) THEN
    ALTER TABLE ads DROP CONSTRAINT ads_status_check;
  END IF;

  ALTER TABLE ads
    ADD CONSTRAINT ads_status_check CHECK (
      status IN (
        'DRAFT',
        'UNDER_MODERATION',
        'ACTIVE',
        'BOOKED',
        'REJECTED',
        'ARCHIVED',
        'DELETED'
      )
    );
END $$
^^

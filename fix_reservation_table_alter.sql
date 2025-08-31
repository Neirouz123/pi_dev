-- Alternative: Fix existing reservation table without dropping it
-- This is safer if you already have data

-- First, check current structure
DESCRIBE reservation;

-- Modify columns if needed
ALTER TABLE reservation 
    MODIFY COLUMN heureDebut TIME NOT NULL,
    MODIFY COLUMN heureFin TIME NOT NULL,
    MODIFY COLUMN prix DECIMAL(10,2) NOT NULL,
    MODIFY COLUMN isConfirmee BOOLEAN DEFAULT FALSE,
    MODIFY COLUMN statut VARCHAR(50) DEFAULT 'En attente',
    MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add foreign key constraints if they don't exist
-- (Remove the lines below if you get errors about existing constraints)

-- Add foreign key for local_id
ALTER TABLE reservation 
    ADD CONSTRAINT fk_reservation_local 
    FOREIGN KEY (local_id) REFERENCES local(id);

-- Add foreign key for utilisateur_id  
ALTER TABLE reservation 
    ADD CONSTRAINT fk_reservation_utilisateur 
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_reservation_local_id ON reservation(local_id);
CREATE INDEX IF NOT EXISTS idx_reservation_utilisateur_id ON reservation(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_reservation_date ON reservation(date);
CREATE INDEX IF NOT EXISTS idx_reservation_statut ON reservation(statut);

-- Show final table structure
DESCRIBE reservation;

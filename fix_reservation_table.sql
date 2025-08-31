-- Fix the reservation table structure
-- Drop the existing table if it exists
DROP TABLE IF EXISTS reservation;

-- Create the reservation table with correct structure
CREATE TABLE reservation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    local_id INT NOT NULL,
    utilisateur_id INT NOT NULL,
    date DATE NOT NULL,
    heureDebut TIME NOT NULL,
    heureFin TIME NOT NULL,
    prix DECIMAL(10,2) NOT NULL,
    isConfirmee BOOLEAN DEFAULT FALSE,
    statut VARCHAR(50) DEFAULT 'En attente',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Add foreign key constraints
    FOREIGN KEY (local_id) REFERENCES local(id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

-- Add indexes for better performance
CREATE INDEX idx_reservation_local_id ON reservation(local_id);
CREATE INDEX idx_reservation_utilisateur_id ON reservation(utilisateur_id);
CREATE INDEX idx_reservation_date ON reservation(date);
CREATE INDEX idx_reservation_statut ON reservation(statut);

-- Insert some sample data (optional)
INSERT INTO reservation (local_id, utilisateur_id, date, heureDebut, heureFin, prix, isConfirmee, statut) VALUES
(1, 1, '2024-06-15', '18:00:00', '23:00:00', 500.00, FALSE, 'En attente'),
(2, 1, '2024-07-20', '14:00:00', '20:00:00', 750.00, TRUE, 'Confirmée');

-- Show the table structure
DESCRIBE reservation;

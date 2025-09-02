<?php

namespace App\Repository;

use App\Entity\Utilisateur;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Utilisateur>
 *
 * @method Utilisateur|null find($id, $lockMode = null, $lockVersion = null)
 * @method Utilisateur|null findOneBy(array $criteria, array $orderBy = null)
 * @method Utilisateur[]    findAll()
 * @method Utilisateur[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class UtilisateurRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Utilisateur::class);
    }

    public function findByEmail(string $email): ?Utilisateur
    {
        return $this->findOneBy(['email' => $email]);
    }

    public function findByUsername(string $username): ?Utilisateur
    {
        return $this->findOneBy(['username' => $username]);
    }

    public function authenticate(string $email, string $password): ?Utilisateur
    {
        $user = $this->findByEmail($email);
        
        // For plain text passwords (temporary - should be hashed in production)
        if ($user && $user->getPasswordHash() === $password) {
            return $user;
        }
        
        return null;
    }

    public function authenticateByUsername(string $username, string $password): ?Utilisateur
    {
        $user = $this->findByUsername($username);
        
        // For plain text passwords (temporary - should be hashed in production)
        if ($user && $user->getPasswordHash() === $password) {
            return $user;
        }
        
        return null;
    }
}

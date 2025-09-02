<?php

namespace App\Repository;

use App\Entity\Local;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Local>
 *
 * @method Local|null find($id, $lockMode = null, $lockVersion = null)
 * @method Local[]    findAll()
 * @method Local[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 * @method Local[]    findOneBy(array $criteria, array $orderBy = null)
 */
class LocalRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Local::class);
    }

    public function findAvailableVenues(): array
    {
        return $this->findBy(['disponible' => true], ['prix' => 'ASC']);
    }

    public function findVenueById(int $id): ?Local
    {
        return $this->find($id);
    }
}

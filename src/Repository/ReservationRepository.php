<?php

namespace App\Repository;

use App\Entity\Reservation;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Reservation>
 *
 * @method Reservation|null find($id, $lockMode = null, $lockVersion = null)
 * @method Reservation[]    findAll()
 * @method Reservation[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 * @method Reservation[]    findOneBy(array $criteria, array $orderBy = null)
 */
class ReservationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Reservation::class);
    }

    /**
     * Get all reservations for a specific user
     */
    public function findUserReservations(int $userId): array
    {
        return $this->createQueryBuilder('r')
            ->leftJoin('r.local', 'l')
            ->addSelect('l')
            ->where('r.utilisateur = :userId') // use relation, not old field
            ->setParameter('userId', $userId)
            ->orderBy('r.date', 'DESC')
            ->addOrderBy('r.heureDebut', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Find a reservation by its ID
     */
    public function findReservationById(int $id): ?Reservation
    {
        return $this->find($id);
    }

    /**
     * Persist a reservation entity
     */
    public function save(Reservation $reservation, bool $flush = false): void
    {
        $em = $this->getEntityManager();
        $em->persist($reservation);

        if ($flush) {
            $em->flush();
        }
    }
}

<?php

namespace App\Test\Controller;

use App\Entity\Reservation;
use App\Repository\ReservationRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\KernelBrowser;
use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class ReservationControllerTest extends WebTestCase
{
    private KernelBrowser $client;
    private ReservationRepository $repository;
    private string $path = '/reservation/controller/c/';
    private EntityManagerInterface $manager;

    protected function setUp(): void
    {
        $this->client = static::createClient();
        $this->repository = static::getContainer()->get('doctrine')->getRepository(Reservation::class);

        foreach ($this->repository->findAll() as $object) {
            $this->manager->remove($object);
        }
    }

    public function testIndex(): void
    {
        $crawler = $this->client->request('GET', $this->path);

        self::assertResponseStatusCodeSame(200);
        self::assertPageTitleContains('Reservation index');

        // Use the $crawler to perform additional assertions e.g.
        // self::assertSame('Some text on the page', $crawler->filter('.p')->first());
    }

    public function testNew(): void
    {
        $originalNumObjectsInRepository = count($this->repository->findAll());

        $this->markTestIncomplete();
        $this->client->request('GET', sprintf('%snew', $this->path));

        self::assertResponseStatusCodeSame(200);

        $this->client->submitForm('Save', [
            'reservation[date]' => 'Testing',
            'reservation[heureDebut]' => 'Testing',
            'reservation[heureFin]' => 'Testing',
            'reservation[prix]' => 'Testing',
            'reservation[isConfirmee]' => 'Testing',
            'reservation[statut]' => 'Testing',
            'reservation[createdAt]' => 'Testing',
            'reservation[local]' => 'Testing',
            'reservation[utilisateur]' => 'Testing',
        ]);

        self::assertResponseRedirects('/reservation/controller/c/');

        self::assertSame($originalNumObjectsInRepository + 1, count($this->repository->findAll()));
    }

    public function testShow(): void
    {
        $this->markTestIncomplete();
        $fixture = new Reservation();
        $fixture->setDate('My Title');
        $fixture->setHeureDebut('My Title');
        $fixture->setHeureFin('My Title');
        $fixture->setPrix('My Title');
        $fixture->setIsConfirmee('My Title');
        $fixture->setStatut('My Title');
        $fixture->setCreatedAt('My Title');
        $fixture->setLocal('My Title');
        $fixture->setUtilisateur('My Title');

        $this->manager->persist($fixture);
        $this->manager->flush();

        $this->client->request('GET', sprintf('%s%s', $this->path, $fixture->getId()));

        self::assertResponseStatusCodeSame(200);
        self::assertPageTitleContains('Reservation');

        // Use assertions to check that the properties are properly displayed.
    }

    public function testEdit(): void
    {
        $this->markTestIncomplete();
        $fixture = new Reservation();
        $fixture->setDate('My Title');
        $fixture->setHeureDebut('My Title');
        $fixture->setHeureFin('My Title');
        $fixture->setPrix('My Title');
        $fixture->setIsConfirmee('My Title');
        $fixture->setStatut('My Title');
        $fixture->setCreatedAt('My Title');
        $fixture->setLocal('My Title');
        $fixture->setUtilisateur('My Title');

        $this->manager->persist($fixture);
        $this->manager->flush();

        $this->client->request('GET', sprintf('%s%s/edit', $this->path, $fixture->getId()));

        $this->client->submitForm('Update', [
            'reservation[date]' => 'Something New',
            'reservation[heureDebut]' => 'Something New',
            'reservation[heureFin]' => 'Something New',
            'reservation[prix]' => 'Something New',
            'reservation[isConfirmee]' => 'Something New',
            'reservation[statut]' => 'Something New',
            'reservation[createdAt]' => 'Something New',
            'reservation[local]' => 'Something New',
            'reservation[utilisateur]' => 'Something New',
        ]);

        self::assertResponseRedirects('/reservation/controller/c/');

        $fixture = $this->repository->findAll();

        self::assertSame('Something New', $fixture[0]->getDate());
        self::assertSame('Something New', $fixture[0]->getHeureDebut());
        self::assertSame('Something New', $fixture[0]->getHeureFin());
        self::assertSame('Something New', $fixture[0]->getPrix());
        self::assertSame('Something New', $fixture[0]->getIsConfirmee());
        self::assertSame('Something New', $fixture[0]->getStatut());
        self::assertSame('Something New', $fixture[0]->getCreatedAt());
        self::assertSame('Something New', $fixture[0]->getLocal());
        self::assertSame('Something New', $fixture[0]->getUtilisateur());
    }

    public function testRemove(): void
    {
        $this->markTestIncomplete();

        $originalNumObjectsInRepository = count($this->repository->findAll());

        $fixture = new Reservation();
        $fixture->setDate('My Title');
        $fixture->setHeureDebut('My Title');
        $fixture->setHeureFin('My Title');
        $fixture->setPrix('My Title');
        $fixture->setIsConfirmee('My Title');
        $fixture->setStatut('My Title');
        $fixture->setCreatedAt('My Title');
        $fixture->setLocal('My Title');
        $fixture->setUtilisateur('My Title');

        $this->manager->persist($fixture);
        $this->manager->flush();

        self::assertSame($originalNumObjectsInRepository + 1, count($this->repository->findAll()));

        $this->client->request('GET', sprintf('%s%s', $this->path, $fixture->getId()));
        $this->client->submitForm('Delete');

        self::assertSame($originalNumObjectsInRepository, count($this->repository->findAll()));
        self::assertResponseRedirects('/reservation/controller/c/');
    }
}

<?php

namespace App\Test\Controller;

use App\Entity\Local;
use App\Repository\LocalRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\KernelBrowser;
use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

class LocalControllerTest extends WebTestCase
{
    private KernelBrowser $client;
    private LocalRepository $repository;
    private string $path = '/local/';
    private EntityManagerInterface $manager;

    protected function setUp(): void
    {
        $this->client = static::createClient();
        $this->repository = static::getContainer()->get('doctrine')->getRepository(Local::class);

        foreach ($this->repository->findAll() as $object) {
            $this->manager->remove($object);
        }
    }

    public function testIndex(): void
    {
        $crawler = $this->client->request('GET', $this->path);

        self::assertResponseStatusCodeSame(200);
        self::assertPageTitleContains('Local index');

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
            'local[nom]' => 'Testing',
            'local[adresse]' => 'Testing',
            'local[description]' => 'Testing',
            'local[capacite]' => 'Testing',
            'local[prix]' => 'Testing',
            'local[disponible]' => 'Testing',
            'local[created_at]' => 'Testing',
            'local[updated_at]' => 'Testing',
        ]);

        self::assertResponseRedirects('/local/');

        self::assertSame($originalNumObjectsInRepository + 1, count($this->repository->findAll()));
    }

    public function testShow(): void
    {
        $this->markTestIncomplete();
        $fixture = new Local();
        $fixture->setNom('My Title');
        $fixture->setAdresse('My Title');
        $fixture->setDescription('My Title');
        $fixture->setCapacite('My Title');
        $fixture->setPrix('My Title');
        $fixture->setDisponible('My Title');
        $fixture->setCreated_at('My Title');
        $fixture->setUpdated_at('My Title');

        $this->manager->persist($fixture);
        $this->manager->flush();

        $this->client->request('GET', sprintf('%s%s', $this->path, $fixture->getId()));

        self::assertResponseStatusCodeSame(200);
        self::assertPageTitleContains('Local');

        // Use assertions to check that the properties are properly displayed.
    }

    public function testEdit(): void
    {
        $this->markTestIncomplete();
        $fixture = new Local();
        $fixture->setNom('My Title');
        $fixture->setAdresse('My Title');
        $fixture->setDescription('My Title');
        $fixture->setCapacite('My Title');
        $fixture->setPrix('My Title');
        $fixture->setDisponible('My Title');
        $fixture->setCreated_at('My Title');
        $fixture->setUpdated_at('My Title');

        $this->manager->persist($fixture);
        $this->manager->flush();

        $this->client->request('GET', sprintf('%s%s/edit', $this->path, $fixture->getId()));

        $this->client->submitForm('Update', [
            'local[nom]' => 'Something New',
            'local[adresse]' => 'Something New',
            'local[description]' => 'Something New',
            'local[capacite]' => 'Something New',
            'local[prix]' => 'Something New',
            'local[disponible]' => 'Something New',
            'local[created_at]' => 'Something New',
            'local[updated_at]' => 'Something New',
        ]);

        self::assertResponseRedirects('/local/');

        $fixture = $this->repository->findAll();

        self::assertSame('Something New', $fixture[0]->getNom());
        self::assertSame('Something New', $fixture[0]->getAdresse());
        self::assertSame('Something New', $fixture[0]->getDescription());
        self::assertSame('Something New', $fixture[0]->getCapacite());
        self::assertSame('Something New', $fixture[0]->getPrix());
        self::assertSame('Something New', $fixture[0]->getDisponible());
        self::assertSame('Something New', $fixture[0]->getCreated_at());
        self::assertSame('Something New', $fixture[0]->getUpdated_at());
    }

    public function testRemove(): void
    {
        $this->markTestIncomplete();

        $originalNumObjectsInRepository = count($this->repository->findAll());

        $fixture = new Local();
        $fixture->setNom('My Title');
        $fixture->setAdresse('My Title');
        $fixture->setDescription('My Title');
        $fixture->setCapacite('My Title');
        $fixture->setPrix('My Title');
        $fixture->setDisponible('My Title');
        $fixture->setCreated_at('My Title');
        $fixture->setUpdated_at('My Title');

        $this->manager->persist($fixture);
        $this->manager->flush();

        self::assertSame($originalNumObjectsInRepository + 1, count($this->repository->findAll()));

        $this->client->request('GET', sprintf('%s%s', $this->path, $fixture->getId()));
        $this->client->submitForm('Delete');

        self::assertSame($originalNumObjectsInRepository, count($this->repository->findAll()));
        self::assertResponseRedirects('/local/');
    }
}

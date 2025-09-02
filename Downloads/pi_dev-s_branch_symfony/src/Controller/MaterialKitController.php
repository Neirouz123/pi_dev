<?php

namespace App\Controller;

use App\Entity\Reservation;
use App\Repository\UtilisateurRepository;
use App\Repository\LocalRepository;
use App\Repository\ReservationRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Annotation\Route;

class MaterialKitController extends AbstractController
{
    #[Route('/', name: 'app_home')]
    public function home(
        Request $request,
        SessionInterface $session,
        UtilisateurRepository $userRepo,
        LocalRepository $localRepo,
        ReservationRepository $reservationRepo
    ): Response {
        $user = null;
        $error = null;
        $success = null;
        $userReservations = [];

        // ------------------- SIGN IN -------------------
        if ($request->isMethod('POST') && $request->request->has('signin')) {
            $email = $request->request->get('email');
            $password = $request->request->get('password');

            $user = $userRepo->authenticate($email, $password);

            if ($user) {
                $session->set('user_id', $user->getId());
                $session->set('user_email', $user->getEmail());
                $session->set('user_username', $user->getUsername());
                $session->set('user_role', $user->getRole());
                $success = 'Successfully signed in!';
            } else {
                $error = 'Invalid email or password';
            }
        }

        // ------------------- SIGN OUT -------------------
        if ($request->request->has('signout')) {
            $session->clear();
            $user = null;
            $success = 'Successfully signed out!';
        }

        // ------------------- BOOK VENUE -------------------
        if ($request->isMethod('POST') && $request->request->has('book_venue')) {
            if (!$session->has('user_id')) {
                $error = 'Please sign in to book a venue';
            } else {
                $venueId = $request->request->get('venue_id');
                $date = $request->request->get('date');
                $heureDebut = $request->request->get('heure_debut');
                $heureFin = $request->request->get('heure_fin');
                $prix = $request->request->get('prix');

                if (!$venueId || !$date || !$heureDebut || !$heureFin || !$prix) {
                    $error = 'Please fill in all required fields';
                } else {
                    try {
                        // Fetch related entities
                        $local = $localRepo->find($venueId);
                        $userEntity = $userRepo->find($session->get('user_id'));

                        if (!$local || !$userEntity) {
                            $error = 'Invalid venue or user';
                        } else {
                            // Create reservation
                            $reservation = new Reservation();
                            $reservation->setLocal($local);
                            $reservation->setUtilisateur($userEntity);
                            $reservation->setDate(new \DateTime($date));
                            $reservation->setHeureDebut(new \DateTime($heureDebut));
                            $reservation->setHeureFin(new \DateTime($heureFin));
                            $reservation->setPrix((string)$prix);
                            $reservation->setIsConfirmee(false);
                            $reservation->setStatut('En attente');
                            $reservation->setCreatedAt(new \DateTime());

                            $reservationRepo->save($reservation, true);

                            $success = 'Venue booked successfully! We will contact you soon.';
                        }
                    } catch (\Exception $e) {
                        $error = 'Error saving reservation: ' . $e->getMessage();
                    }
                }
            }
        }

        // ------------------- GET CURRENT USER & RESERVATIONS -------------------
        if ($session->has('user_id')) {
            $user = $userRepo->find($session->get('user_id'));
            $userReservations = $reservationRepo->findUserReservations($user->getId());
        }

        // ------------------- GET AVAILABLE VENUES -------------------
        $venues = $localRepo->findAvailableVenues();

        return $this->render('page/index.html.twig', [
            'user' => $user,
            'error' => $error,
            'success' => $success,
            'venues' => $venues,
            'userReservations' => $userReservations
        ]);
    }

    #[Route('/cart-demo', name: 'app_cart_demo')]
    public function cartDemo(): Response
    {
        return $this->render('page/cart-demo.html.twig');
    }

    #[Route('/logout', name: 'app_logout')]
    public function logout(SessionInterface $session): Response
    {
        $session->clear();
        $this->addFlash('success', 'Successfully signed out!');
        return $this->redirectToRoute('app_home');
    }
}
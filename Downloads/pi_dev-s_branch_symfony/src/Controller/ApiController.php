<?php 
// src/Controller/ApiController.php
namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Psr\Log\LoggerInterface;

class ApiController extends AbstractController
{
    private const TRANSLATION_SERVICES = [
        'libretranslate' => [
            'url' => 'https://libretranslate.de/translate',
            'method' => 'POST',
            'options' => [
                'headers' => ['Content-Type' => 'application/json'],
                'json' => [
                    'q' => '',
                    'source' => 'fr',
                    'target' => 'en',
                    'format' => 'text'
                ],
                'timeout' => 5
            ]
        ],
        'mymemory' => [
            'url' => 'https://api.mymemory.translated.net/get',
            'method' => 'GET',
            'options' => [
                'query' => [
                    'q' => '',
                    'langpair' => 'fr|en'
                ],
                'timeout' => 5
            ]
        ]
    ];

    public function __construct(
        private HttpClientInterface $httpClient,
        private LoggerInterface $logger
    ) {}

    #[Route('/api/translate', name: 'api_translate', methods: ['POST'])]
    public function translate(Request $request): JsonResponse
    {
        // Validation de la requête
        if (empty($request->getContent())) {
            return new JsonResponse(
                ['error' => 'Request body is empty'],
                JsonResponse::HTTP_BAD_REQUEST
            );
        }

        try {
            $data = json_decode($request->getContent(), true, 512, JSON_THROW_ON_ERROR);
        } catch (\JsonException $e) {
            return new JsonResponse(
                ['error' => 'Invalid JSON format'],
                JsonResponse::HTTP_BAD_REQUEST
            );
        }

        if (!isset($data['text']) || !is_string($data['text'])) {
            return new JsonResponse(
                ['error' => 'Text parameter is required and must be a string'],
                JsonResponse::HTTP_BAD_REQUEST
            );
        }

        $text = trim($data['text']);
        if (empty($text)) {
            return new JsonResponse(
                ['error' => 'Text cannot be empty'],
                JsonResponse::HTTP_BAD_REQUEST
            );
        }

        // Tentative de traduction
        foreach (self::TRANSLATION_SERVICES as $serviceName => $service) {
            try {
                $config = $service;
                $paramKey = $config['method'] === 'POST' ? 'json' : 'query';
                $config['options'][$paramKey]['q'] = $text;

                $this->logger->info('Attempting translation with service', [
                    'service' => $serviceName,
                    'text_length' => strlen($text)
                ]);

                $response = $this->httpClient->request(
                    $config['method'],
                    $config['url'],
                    $config['options']
                );

                $statusCode = $response->getStatusCode();
                if ($statusCode !== 200) {
                    throw new \RuntimeException("API returned status code: $statusCode");
                }

                $result = $response->toArray();
                $translated = $this->extractTranslation($result, $service['url']);

                if (!$translated) {
                    throw new \RuntimeException('No translation found in response');
                }

                if ($translated === $text) {
                    throw new \RuntimeException('Translation identical to original text');
                }

                $this->logger->info('Translation successful', [
                    'service' => $serviceName,
                    'original' => $text,
                    'translated' => $translated
                ]);

                return new JsonResponse([
                    'translatedText' => $translated,
                    'service' => $serviceName,
                    'success' => true
                ]);

            } catch (\Exception $e) {
                $this->logger->error('Translation attempt failed', [
                    'service' => $serviceName,
                    'error' => $e->getMessage(),
                    'trace' => $e->getTraceAsString()
                ]);
                continue;
            }
        }

        return new JsonResponse(
            [
                'error' => 'All translation services failed',
                'originalText' => $text,
                'success' => false
            ],
            JsonResponse::HTTP_SERVICE_UNAVAILABLE
        );
    }

    private function extractTranslation(array $data, string $serviceUrl): ?string
    {
        $host = parse_url($serviceUrl, PHP_URL_HOST);
        
        switch ($host) {
            case 'libretranslate.de':
                return $data['translatedText'] ?? null;
            case 'api.mymemory.translated.net':
                return $data['responseData']['translatedText'] ?? null;
            default:
                return null;
        }
    }
}
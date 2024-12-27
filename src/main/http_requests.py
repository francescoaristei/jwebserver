import concurrent.futures
import requests

def fetch_url(url):
    response = requests.get(url)
    return response.text

if __name__ == '__main__':
    url = 'http://localhost/'
    concurrent_requests = 20
    results = []
    futures = []

    with concurrent.futures.ThreadPoolExecutor() as executor:
        for i in range(concurrent_requests):
            future = executor.submit(fetch_url, url)
            futures.append(future)

    results = [future.result() for future in futures]

    for i, result in enumerate(results):
        print(f'Response {i + 1} from {url}: {result[:100]}')

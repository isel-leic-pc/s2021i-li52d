using System;
using System.Linq;
using System.Threading.Tasks;
using System.Net.Http;

namespace Aula22_async_methods {
    using  generators.async;
    using static generators.async.AsyncGenerators;

    class Program {
        public static Task<string> DownloadUrlContentAsStringAsync(string uri) {
            HttpClient client = new HttpClient();
            return client.GetStringAsync(uri);
        }

        public static void TestAsyncEnumerator() {
            Task<int> task = TwoIncrementsAsync().Run();
            Console.WriteLine(task.Result);
        }

        public static void TestAsyncEnumerator2() {
            string[] urls = {
                "https://www.rtp.pt",
                "https://www.isel.pt",
                "http://www.microsoft.com"
            };

            var tasks = urls
                        .Select(u => DownloadUrlContentAsStringAsync(u))
                        .ToList();

            Task done = ProcessAsReceivedAsync(
                    tasks, s => {
                Console.WriteLine(s);
            }).Run();

            done.Wait();
        }

        static void Main(string[] args) {
           

            TestAsyncEnumerator2();
        }
    }
}

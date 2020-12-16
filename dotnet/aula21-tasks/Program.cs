using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Net.Http;

namespace aula21_tasks {
    class Program {


        public static Task<string> DownloadUrlContentAsStringAsync(string uri) {
            HttpClient client = new HttpClient();
            return client.GetStringAsync(uri);
        }

        static Task ProcessCompletedTasksAsync(List<Task<string>> tasks,
        Action<string> processor) {

        TaskCompletionSource<bool> promise = new TaskCompletionSource<bool>();

            void continuation() {
            Task.WhenAny(tasks)
            .ContinueWith(ant => {
                processor(ant.Result.Result);
                tasks.Remove(ant.Result);
                if (tasks.Count > 0)
                    continuation();
                else
                    promise.SetResult(true);
                });
            }

            continuation();

            return promise.Task;       
            
        }


        static void Main(string[] args) {
            string[] urls = {
                "https://www.rtp.pt",
                "https://www.isel.pt",
                "http://www.microsoft.com"
            };

            var tasks = urls
                        .Select(u => DownloadUrlContentAsStringAsync(u))
                        .ToList();

            Console.WriteLine(tasks.GetType());

            ProcessCompletedTasksAsync(tasks,
                s => {
                    Console.WriteLine(s);
                    Console.WriteLine();
                })
            .Wait();

            Console.WriteLine("Done!");
        }
    }
}

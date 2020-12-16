using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace AsyncUtils {
    public class AsyncOpers {
        /// <summary>
        /// A possible implementation of an asynchronous stream copy
        /// </summary>
        /// <param name="dst"></param>
        /// <param name="src"></param>
        /// <returns></returns>
        public static Task<int> CopyStreamAsync(Stream dst, Stream src) {
            const int MAXBUF = 4096;

            var promise = new TaskCompletionSource<int>();
            byte[] buffer = new byte[MAXBUF];
            int totalBytes = 0;

            // use of C# 7.0 local function instead of declaring a lambda!
            void cont(Task ant1) {
                if (ant1 != null && ant1.IsFaulted) {
                    promise.SetException(ant1.Exception);
                    return;
                }
                src.ReadAsync(buffer, 0, MAXBUF).
                ContinueWith(ant2 => {
                    if (ant2.IsFaulted) {
                        promise.SetException(ant2.Exception);
                        return;
                    }
                    int nr = ant2.Result;
                    if (nr == 0)
                        promise.SetResult(totalBytes);
                    else {
                        totalBytes += nr;
                        dst.WriteAsync(buffer, 0, 4096).ContinueWith(cont);
                    }
                });
            }

            cont(null);
            return promise.Task;
        }

        public static async Task<int> CopyStream2Async(Stream dst, Stream src) {
            byte[] buffer= new byte[4096];

            int nread, total = 0;

            while ((nread = await src.ReadAsync(buffer,0, 4096)) > 0 ) {
                await dst.WriteAsync(buffer, 0, nread);
                total += nread;
            }

            return total;

        }

        public static async Task<int> TestAsync() {
            return 2;
        }
    }
}

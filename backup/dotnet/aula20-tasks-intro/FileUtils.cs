using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading.Tasks;

namespace aula20_tasks_intro {
    class FileUtils {

        public static Task<long> CopyFileAsync(string fin, string fout) {
            FileStream fsi = new FileStream(fin, FileMode.Open);

            FileStream fso = new FileStream(fout, FileMode.Create);

            return fsi.CopyToAsync(fso)
                .ContinueWith(t => {
                    if (t.Status != TaskStatus.RanToCompletion) {
                        if (t.IsFaulted) throw t.Exception;
                        throw new IOException();
                    }
                    return fsi.Length;
                });
        }


        public static Task<long> Copy2FilesAsync(string fin1, string fout1,
                           string fin2, string fout2) {
            Task<long> t1 = CopyFileAsync(fin1, fout1);
            Task<long> t2 = CopyFileAsync(fin2, fout2);

            return t1.ContinueWith(ant => {
                long l1 = ant.Result;
                return t2.ContinueWith(ant2 => {
                    long l2 = ant2.Result;
                    return l1 + l2;
                });
            }).Unwrap();
        }

        public static Task<long> Copy2FilesAsyncNew(string fin1, string fout1,
                         string fin2, string fout2) {

            TaskCompletionSource<long> promise = new TaskCompletionSource<long>();

            Task<long> t1 = CopyFileAsync(fin1, fout1);
            Task<long> t2 = CopyFileAsync(fin2, fout2);

            var t = t1.ContinueWith(ant1 => {
                return t2.ContinueWith(ant2 => {
                    if (ant1.IsFaulted || ant2.IsFaulted)
                        promise.SetException(new Exception("CopyOperation Failed!"));
                    else
                        promise.SetResult(ant1.Result + ant2.Result);
                });
            });

            return promise.Task;
        }

     
    }
}

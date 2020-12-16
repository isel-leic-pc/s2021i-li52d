using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace Aula24_gui_and_async {
    using static AsyncUtils.AsyncEnumerator;

    public static class AsyncIteratorsModel {

        /// <summary>
        /// An asynchronous iterator example: copying a stream using an asynchronous iterator.
        /// An asynchronous iterator is an iterator that returns an enumeration of tasks. Note 
        /// that, courtesy of yield return and yield break, we can write 
        /// a code very similarly to a synchronous implementation. 
        /// But we must use a runner (async enumerator) of this iterator as showed in the next method.
        /// </summary>
        /// <param name="src"></param>
        /// <returns></returns>
        private static IEnumerable<Task> CopyToMemoryStreamInternalAsyncGenerator(Stream src) {
            const int MAXBUF = 4096;

            byte[] buffer = new byte[MAXBUF];
            MemoryStream ms = new MemoryStream();

            while (true) {
                Task<int> tr;
                yield return tr = src.ReadAsync(buffer, 0, 4096);
                if (tr.Result == 0) {
                    yield return Task.FromResult(ms);
                    yield break;
                }
                ms.Write(buffer, 0, tr.Result);
            }
        }

        /// <summary>
        /// The runner for the previous iterator using the Run method of the AsyncEnumerator class
        /// </summary>
        /// <param name="src"></param>
        /// <returns></returns>
        public static Task<MemoryStream> CopyToMemoryStreamAsyncGenerator(this Stream src) {
            return CopyToMemoryStreamInternalAsyncGenerator(src).Run<MemoryStream>();
        }

        /// <summary>
        /// An async iterator for an alternative (just for pedagogycal purposes) implementation
        /// of the asyncnronous image download, using the runner in the next method
        /// </summary>
        /// <param name="url"></param>
        /// <returns></returns>
        private static IEnumerable<Task> DownloadImageFromUrlAsyncGeneratorInternal(String url) {
            HttpClient client = new HttpClient();
            MemoryStream ms = new MemoryStream();

            Task<Stream> ts;
            yield return ts = client.GetStreamAsync(url);

            yield return ts.Result.CopyToAsync(ms);

            yield return Task.FromResult(Image.FromStream(ms));
        }

        /// <summary>
        /// the runner of the iterator presented in the previous method
        /// </summary>
        /// <param name="url"></param>
        /// <returns></returns>
        public static Task<Image> DownloadImageFromUrlAsyncGenerator(string url) {
            return DownloadImageFromUrlAsyncGeneratorInternal(url).Run<Image>();
        }

    }
}

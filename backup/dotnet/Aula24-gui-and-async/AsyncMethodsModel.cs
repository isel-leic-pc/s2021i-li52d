using System;
using System.IO;
using System.Net.Http;
using System.Drawing;
using System.Threading.Tasks;

namespace Aula24_gui_and_async {
    class AsyncMethodsModel {

        /// <summary>
        /// An alternative using async/await.
        /// To be presented in next lecture (11/12/2018)
        /// </summary>
        /// <param name="url"></param>
        /// <returns></returns>
        public static async Task<Image> DownloadImageFromUrlAsyncMethod(String url) {
            HttpClient client = new HttpClient();

            Stream s = await client.GetStreamAsync(url);
            Stream ms = new MemoryStream();
            await s.CopyToAsync(ms);

            return Image.FromStream(ms);
        }
    }
}

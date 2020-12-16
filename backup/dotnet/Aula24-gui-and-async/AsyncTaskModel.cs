
using AsyncUtils;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Net.Http;
using System.Threading.Tasks;

using System.Threading;
using System.Linq;

namespace Aula24_gui_and_async {

    static class AsyncTaskModel {

        /// <summary>
        /// A solution for async image download operation.
        /// Try to describe why is the Unwrap necessary!
        /// 
        /// More alternatives exists on Model1 class
        /// </summary>
        /// <param name="url"></param>
        /// <returns></returns>
        public static Task<Image> DownloadImageFromUrlAsync(String url) {
            HttpClient client = new HttpClient();
            //client.DefaultRequestHeaders.Add("User-Agent", "Mozilla/5.0");
          
            return client.GetStreamAsync(url).
                ThenCompose(s => {
                    MemoryStream ms = new MemoryStream();
                    return s.CopyToAsync(ms).ContinueWith(_ => ms);
                }).
                ContinueWith(ant2 => {
                    return Image.FromStream(ant2.Result);
                });

        }

      


    }
}

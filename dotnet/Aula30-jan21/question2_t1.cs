using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;

namespace training2021i
{
	public class question2_t1
	{
        public class DevState { }
        public class DevAddr { }
        public class DevReport{ }
        public class DevInfo
		{
            public DevInfo(DevState state, DevReport report) { }
		}

        public interface Service
		{
            DevState GetKnownDevState(int devId);
            DevAddr GetDevAddr(int devId);
            DevReport GetCurrDevReport(DevAddr addr);
        }

        public interface ServiceAsync
        {
            Task<DevState> GetKnownDevStateAsync(int devId);
            Task<DevAddr> GetDevAddrAsync(int devId);
            Task<DevReport> GetCurrDevReportAsync(DevAddr addr);
        }

        public class Control
        {

            public static Task<DevInfo> CheckDeviceAsync(Service svc, int devId) {
                DevState lastKnownState = null;
                Task.Run(() => { lastKnownState = svc.GetKnownDevState(devId); });
                return Task.Run(() => {
                    DevAddr addr = svc.GetDevAddr(devId);
                    DevReport devReport = svc.GetCurrDevReport(addr);
                    return new DevInfo(lastKnownState, devReport);

                });
            }

            public static async Task<DevInfo> CheckDevice2Async(ServiceAsync svc, int devId) {
                
                var lastKnownState = svc.GetKnownDevStateAsync(devId);  
                
                DevAddr addr = await svc.GetDevAddrAsync(devId);
                DevReport devReport = await svc.GetCurrDevReportAsync(addr);
                return new DevInfo(await lastKnownState, devReport);
            }

            public static  Task<DevInfo> CheckDevice3Async(ServiceAsync svc, int devId) {
               
                var lastKnownState = svc.GetKnownDevStateAsync(devId);

                var t2 = svc.GetDevAddrAsync(devId)
                .ContinueWith(ant => svc.GetCurrDevReportAsync(ant.Result))
                .Unwrap();

                return Task.WhenAll(lastKnownState, t2)
                .ContinueWith(_ => new DevInfo(lastKnownState.Result, t2.Result));
                
            }


        }
    }
}

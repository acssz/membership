(() => {
    if ("serviceWorker" in navigator) {
        window.addEventListener("load", () => {
            navigator.serviceWorker.register("/service-worker.js").catch(() => {
                // 在开发环境中忽略 Service Worker 注册失败
            });
        });
    }
})();

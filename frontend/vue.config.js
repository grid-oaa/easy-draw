module.exports = {
  publicPath: '/draw/',
  devServer: {
    port: 5173,
    proxy: {
      '/drawio/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
};

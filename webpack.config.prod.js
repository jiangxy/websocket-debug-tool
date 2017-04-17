const webpack = require('webpack');

const CompressionPlugin = require("compression-webpack-plugin");
const HtmlWebpackPlugin = require('html-webpack-plugin');

const babelLoaderConfig = {
  presets: ['latest', 'stage-0', 'react'],
  plugins: [['import', {libraryName: 'antd', style: true}]],
  cacheDirectory: true,
};

module.exports = {
  devtool: 'cheap-module-source-map',

  entry: [
    'babel-polyfill',
    './src/index.js',
  ],

  output: {
    path: __dirname + '/dist',
    filename: 'bundle.min.js',
  },

  resolve: {
    modulesDirectories: ['node_modules', './src'],
    extensions: ['', '.js', '.jsx'],
  },

  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        // 删除一些debug语句
        loaders: ['babel-loader?' + JSON.stringify(babelLoaderConfig), 'strip-loader?strip[]=logger.info,strip[]=logger.debug,strip[]=console.log,strip[]=console.debug'],
        exclude: /node_modules/,
      }, {
        test: /\.css$/,
        loader: 'style!css',
      }, {
        test: /\.less$/,
        loader: 'style!css!' + `less?{"sourceMap":true}`,
      }, {
        test: /\.(png|jpg|svg)$/,
        loader: 'url?limit=25000',
      },
    ],
  },

  // 减小bundle size是个很大的学问...
  // https://chrisbateman.github.io/webpack-visualizer/
  // http://stackoverflow.com/questions/34239731/how-to-minimize-the-size-of-webpacks-bundle
  // https://webpack.github.io/docs/code-splitting.html
  // http://survivejs.com/webpack/building-with-webpack/splitting-bundles/

  plugins: [
    // 代码压缩
    new webpack.optimize.UglifyJsPlugin({
      sourceMap: true,
      minimize: true,
      compress: {warnings: false},
      output: {comments: false},
    }),

    new HtmlWebpackPlugin({
      template: 'index.html.template',
      title: 'WebSocket Debug Tool',
      favIcon: 'http://jxy.me/favicon.ico',
      hash: true,  // 引入js/css的时候加个hash, 防止cdn的缓存问题
      minify: {removeComments: true, collapseWhitespace: true},
    }),

    new webpack.optimize.DedupePlugin(),
    // 比对id的使用频率和分布来得出最短的id分配给使用频率高的模块
    new webpack.optimize.OccurenceOrderPlugin(),
    new webpack.optimize.AggressiveMergingPlugin(),
    // 允许错误不打断程序
    new webpack.NoErrorsPlugin(),

    // 压缩成gzip格式
    new CompressionPlugin({
      asset: "[path].gz[query]",
      algorithm: "gzip",
      test: /\.js$|\.css$|\.html$/,
      threshold: 10240,
      minRatio: 0,
    }),

    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV),
      __DEV__: JSON.stringify(JSON.parse(process.env.NODE_ENV === 'production' ? 'false' : 'true')),
    }),
  ],
};

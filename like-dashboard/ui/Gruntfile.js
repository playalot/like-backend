'use strict';

var livereload = require('connect-livereload'),
    path = require('path');

module.exports = function (grunt) {

  // show elapsed time at the end
  require('time-grunt')(grunt);

  // load all grunt tasks
  require('load-grunt-tasks')(grunt);

  var yeomanConfig = {
      app: 'app',
      dist: 'dist'
  };

  grunt.initConfig({
      yeoman: yeomanConfig,
      connect: {
          options: {
            port: 3000,
            hostname: '0.0.0.0' //change to 'localhost' to disable outside connections
          },
          livereload: {
            options: {
              middleware: function (connect) {
                return [
                  livereload({port: 35729}),
                  connect.static(path.resolve('.tmp')),
                  connect.static(path.resolve(yeomanConfig.app))
                ];
              }
            }
          }
      },
      watch: {
        options: {
          livereload: 35729
        },
        less: {
          files: ['app/less/*.less'],
          tasks: ['less:dev']
        },
        react: {
          files: ['<%= yeoman.app %>/scripts/**/*.{jsx,js}'],
          tasks: ['browserify:dev']
        },
        images: {
          files: [
            '<%= yeoman.app %>/*.html',
            '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
          ]
        }
      },
      clean: {
        dist: ['.tmp', '<%= yeoman.dist %>/*'],
        serve: '.tmp'
      },
      less: {
        dev: {
          options: {
            compress: true,
            paths: ['app/vendor/bootstrap/less', 'app/styles']
          },
          files: {
              'app/styles/main.css': 'app/less/main.less'
          }
        }
      },
      browserify: {
        options: {
          transform: ['reactify']
        },
        dist: {
          files: {
            '.tmp/scripts/bundle/app.js': '<%= yeoman.app %>/scripts/app.js'
          },
          options: {
            browserifyOptions: {
              extensions: '.jsx'
            }
          }
        },
        dev: {
          files: {
            '.tmp/scripts/bundle/app.js': '<%= yeoman.app %>/scripts/app.js',
          },
          options: {
            browserifyOptions: {
              debug: true,
              extensions: '.jsx'
            }
          }
        }
      },
      useminPrepare: {
        src: '<%= yeoman.app %>/index.html',
        options: {
            dest: '<%= yeoman.dist %>'
        }
      },
      imagemin: {
        dist: {
          files: [{
            expand: true,
            cwd: '<%= yeoman.app %>/images',
            src: '{,*/}*.{png,jpg,jpeg}',
            dest: '<%= yeoman.dist %>/images'
          }]
        }
      },
      htmlmin: {
        dist: {
          options: {
            //removeCommentsFromCDATA: true,
            // https://github.com/yeoman/grunt-usemin/issues/44
            collapseWhitespace: true,
            collapseBooleanAttributes: true,
            //removeAttributeQuotes: true,
            removeRedundantAttributes: true,
            //useShortDoctype: true,
            removeEmptyAttributes: true,
            //removeOptionalTags: true
          },
          files: [{
            expand: true,
            cwd: '<%= yeoman.dist %>',
            src: '*.html',
            dest: '<%= yeoman.dist %>'
          }]
        }
      },
      filerev: {
        dist: {
          files: [{
            src: [
              '<%= yeoman.dist %>/scripts/**/*.js',
              '<%= yeoman.dist %>/styles/**/*.css',
              '<%= yeoman.dist %>/vendor/**/*.js'
            ]
          }]
        }
      },
      autoprefixer: {
        options: {
          browsers: [
            'last 5 versions'
          ]
        },
        dist: {
          expand: true,
          src: '.tmp/concat/styles/*.css'
        }
      },
      copy: {
        fonts: {
          expand: true,
          src: 'vendor/bootstrap/dist/fonts/*',
          dest: 'dist/'
        },
        dist: {
          files: [{
            expand: true,
            dot: true,
            cwd: '<%= yeoman.app %>',
            dest: '<%= yeoman.dist %>',
            src: [
              '*.html',
              '*.{ico,txt}',
              'images/{,*/}*.{webp,gif}'
            ]
          }]
        }
      },
      usemin: {
        html: ['<%= yeoman.dist %>/{,*/}*.html'],
        css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
        options: {
          dirs: ['<%= yeoman.dist %>']
        }
      }
  });

  grunt.registerTask('serve', [
    'clean:serve',
    'less:dev',
    'copy:dev',
    'browserify:dev',
    // 'connect:livereload',
    'watch'
  ]);

  grunt.registerTask('build', [
    'clean:dist',
    'browserify:dist',
    'useminPrepare',
    'concat',
    'autoprefixer:dist',
    'imagemin',
    'cssmin',
    'uglify',
    'copy:fonts',
    'copy:dist',
    'filerev',
    'usemin',
    'htmlmin'
  ]);

  grunt.registerTask('default', 'build');
};

var gulp = require('gulp'),
    ts = require('gulp-typescript'),
    sourcemaps = require('gulp-sourcemaps'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    gutil = require('gulp-util'),
    less = require('gulp-less'),
    cleanCSS  = require('gulp-clean-css'),
    rename = require("gulp-rename"),
    templateCache = require('gulp-angular-templatecache'),
    htmlmin = require('gulp-htmlmin'),
    runSequence = require('run-sequence');

var appSourcesDir = 'app';
var appStylesDir = 'styles';

var buildDir = 'dist';
var buildCodeDir = buildDir + '/app';
var buildCssDir = buildDir + '/css';

gulp.task('build', function(callback) {
    runSequence('build-ts',
                'build-templatecache',
                'build-css',
                'copy-static-html',
                'copy-static-images',
                callback);
});

gulp.task('build-ts', function () {
    return gulp.src(appSourcesDir + '/**/*.ts')
               .pipe(gutil.env.buildtype !== 'prod' ? sourcemaps.init() : gutil.noop())
               .pipe(ts({outFile: 'home-min.js',
                         diagnostics: true
                        }))
               .pipe(uglify())
               .pipe(gutil.env.buildtype !== 'prod' ? sourcemaps.write() : gutil.noop())
               .pipe(gulp.dest(buildCodeDir));
});

gulp.task('build-templatecache', function () {
    return gulp.src('app/**/*.html')
               .pipe(htmlmin({collapseWhitespace: true,
                              removeComments: true,
                              collapseBooleanAttributes: true,
                              removeAttributeQuotes: true,
                              removeRedundantAttributes: true,
                              removeEmptyAttributes: true
                             }))
               .pipe(templateCache('templatecache.js', { root: 'app' }))
               .pipe(gulp.dest(buildCodeDir));
});

gulp.task('build-css', function() {
    return gulp.src(appStylesDir + '/main.less')
               .pipe(less())
               .pipe(cleanCSS())
               .pipe(rename('home.css'))
               .pipe(gulp.dest(buildCssDir));
});

gulp.task('copy-static-html', function () {
    return gulp.src(['index.html', 'top-menu.html'])
               .pipe(gulp.dest(buildDir));
});

gulp.task('copy-static-images', function () {
    return gulp.src('images/**/*')
               .pipe(gulp.dest(buildDir));
});

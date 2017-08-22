var gulp = require('gulp'),
    jshint = require('gulp-jshint'),
    sourcemaps = require('gulp-sourcemaps'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    gutil = require('gulp-util'),
    less = require('gulp-less'),
    cleanCSS  = require('gulp-clean-css'),
    rename = require("gulp-rename");

var appSourcesRoot = 'src/main/resources/static';
var appJsDir = appSourcesRoot + '/app';
var appStylesDir = appSourcesRoot + '/styles';

var buildDir = gutil.env.buildtype === 'prod' ? 'build/resources/main/static' : 'out/production/resources/static';

var buildJsDir = buildDir + '/app';
var buildCssDir = buildDir + '/css';

// configure the jshint task
gulp.task('jshint', function() {
    return gulp.src(appJsDir + '/**/*.js')
        .pipe(jshint())
        .pipe(jshint.reporter('jshint-stylish'))
        .pipe(jshint.reporter('fail'));
});

// Task to watch resource changes
gulp.task('watch', ['build', 'watch-js', 'watch-styles']);

// Task to watch style changes
gulp.task('watch-styles', function() {
    gulp.watch(appStylesDir + '/**/*.less', ['build-css']);
});

// Task to watch js changes
gulp.task('watch-js', function() {
    gulp.watch(appJsDir + '/**/*.js', ['jshint', 'build-js']);
});

// Task to build all frontend artifacts
gulp.task('build', ['jshint', 'build-js', 'build-css']);

// Task to build js file
gulp.task('build-js', function() {
    return gulp.src(appJsDir + '/**/*.js')
        .pipe(sourcemaps.init())
        .pipe(concat('home.js'))
        // only uglify if gulp is ran with '--buildtype prod
        .pipe(gutil.env.buildtype === 'prod' ? uglify() : gutil.noop())
        .pipe(sourcemaps.write())
        .pipe(gulp.dest(buildJsDir));
});

// Task to build css file
gulp.task('build-css', function() {
    gulp.src(appStylesDir + '/main.less')
        .pipe(less())
         // only minify if gulp is ran with '--buildtype prod
        .pipe(gutil.env.buildtype === 'prod' ? cleanCSS() : gutil.noop())
        .pipe(rename('home.css'))
        .pipe(gulp.dest(buildCssDir));
});
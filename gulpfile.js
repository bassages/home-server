var gulp = require('gulp'),
    jshint = require('gulp-jshint'),
    sourcemaps = require('gulp-sourcemaps'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    gutil = require('gulp-util'),
    less = require('gulp-less'),
    minifyCSS  = require('gulp-minify-css'),
    rename = require("gulp-rename");

var appSourcesDir = 'src/main/resources/static/app';
var appStylesDir = 'src/main/wro';

var buildJsDir = 'target/classes/static/app';
var buildCssDir = 'target/classes/static/css';

// define the default task and add the watch task to it
gulp.task('default', ['watch-styles', 'watch-js']);

// configure the jshint task
gulp.task('jshint', function() {
    return gulp.src(appSourcesDir + '/**/*.js')
        .pipe(jshint())
        .pipe(jshint.reporter('jshint-stylish'));
});

/* Task to watch style changes */
gulp.task('watch-styles', function() {
    gulp.watch(appStylesDir + '/**/*.less' , ['build-css']);
});

/* Task to watch js-app changes */
gulp.task('watch-js', function() {
    gulp.watch(appSourcesDir + '/**/*.js', ['jshint']);
});

/* Task to build js file */
gulp.task('build-js', function() {
    return gulp.src(appSourcesDir + '/**/*.js')
        .pipe(sourcemaps.init())
        .pipe(concat('home.js'))
        // only uglify if gulp is ran with '--type prod
        .pipe(gutil.env.type === 'prod' ? uglify() : gutil.noop())
        .pipe(sourcemaps.write())
        .pipe(gulp.dest(buildJsDir));
});

gulp.task('build-css', function() {
    gulp.src(appStylesDir + '/main.less')
        .pipe(sourcemaps.init())
        .pipe(less())
        .pipe(minifyCSS())
        .pipe(sourcemaps.write())
        .pipe(rename('home.css'))
        .pipe(gulp.dest(buildCssDir));
});
package com.ramusthastudio.cataloguemovie.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.ramusthastudio.cataloguemovie.BuildConfig;
import com.ramusthastudio.cataloguemovie.MoviesActivity;
import com.ramusthastudio.cataloguemovie.R;
import com.ramusthastudio.cataloguemovie.model.Moviedb;
import com.ramusthastudio.cataloguemovie.model.Result;
import java.util.Random;

import static com.ramusthastudio.cataloguemovie.fragment.AbstractMovieFragment.ARG_PARAM;

public final class UpcomingReminderService extends JobService implements Tasks.TaskListener<Moviedb> {
  private static final int UPCOMING_CODE = 222;
  public static final String UPCOMING_REMIDER_TAG = "UpcomingReminderService";
  public static final String EXTRA_JOB_SERVICE = "extra_job_service";
  private final Tasks<Moviedb> fTasks;

  public UpcomingReminderService() {
    fTasks = new Tasks<>(this);
  }

  @Override public void onStartTask() { }

  @Override
  public boolean onStartJob(JobParameters aJobParameters) {
    Log.d(UpcomingReminderService.class.getSimpleName(), "UpcomingReminderService Executed");
    fTasks.start(aJobParameters);
    return true;
  }

  @Override public boolean onStopJob(JobParameters job) {
    Log.d(UpcomingReminderService.class.getSimpleName(), "UpcomingReminderService Executed");
    return true;
  }

  @Override
  public void onSuccess(Moviedb aResponse, JobParameters aJobParameters) {
    Log.d(MoviesActivity.class.getSimpleName(), aResponse.toString());
    showNotification(getApplicationContext(), aResponse, 100);
    jobFinished(aJobParameters, false);
  }

  @Override
  public void onFailure(int statusCode, Throwable aThrowable, JobParameters aJobParameters) {
    Log.e(MoviesActivity.class.getSimpleName(), String.format("status %s, couse %s", statusCode, aThrowable));
    jobFinished(aJobParameters, false);
  }

  @Override public Class<Moviedb> toClass() { return Moviedb.class; }

  private static void showNotification(final Context context, Moviedb aMoviedb, int notifId) {
    final Result movie = aMoviedb.getResults().get(randomMovie(aMoviedb.getResults().size()));

    NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    Intent notificationIntent = new Intent(context, MoviesActivity.class);
    notificationIntent.putExtra(ARG_PARAM, movie);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

    final NotificationCompat.Builder builder;

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      builder = new NotificationCompat.Builder(context, NotificationChannel.DEFAULT_CHANNEL_ID);
    } else {
      builder = new NotificationCompat.Builder(context);
    }

    final PendingIntent intent = PendingIntent.getActivity(context, UPCOMING_CODE, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.movie_notification);
    remoteViews.setImageViewResource(R.id.notifImageView, R.mipmap.ic_launcher_round);
    remoteViews.setTextViewText(R.id.notifTitleView, movie.getTitle());
    remoteViews.setTextViewText(R.id.notifRatingView, String.valueOf(movie.getVoteAverage()));
    remoteViews.setTextViewText(R.id.notifReleaseView, MovieListAdapter.sDateFormat.format(movie.getReleaseDate()));

    builder.setContentTitle(movie.getTitle())
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContent(remoteViews)
        .setContentIntent(intent)
        .setAutoCancel(true)
        .setSound(alarmSound);

    final Notification notification = builder.build();

    // set big content view for newer androids
    if (android.os.Build.VERSION.SDK_INT >= 16) {
      notification.bigContentView = remoteViews;
    }

    if (notificationManagerCompat != null) {
      notificationManagerCompat.notify(notifId, notification);
    }

    final NotificationTarget notificationTarget = new NotificationTarget(
        context,
        R.id.notifImageView,
        remoteViews,
        notification,
        notifId);

    Glide.with(context.getApplicationContext())
        .asBitmap()
        .load(BuildConfig.IMAGE_URL + "/w342" + movie.getBackdropPath())
        .into(notificationTarget);
  }

  private static int randomMovie(int aSize) {
    Random r = new Random();
    return r.nextInt(aSize - 1);
  }
}
